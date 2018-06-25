package solutions.oneguard.msa.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import solutions.oneguard.msa.core.messaging.AbstractMessageHandler;
import solutions.oneguard.msa.core.model.Message;
import solutions.oneguard.msa.front.controller.websocket.WebSocketMessage;
import solutions.oneguard.msa.front.controller.websocket.WebSocketSessionRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MicroServiceResponseHandler extends AbstractMessageHandler<Object> {
    private static final Logger log = LoggerFactory.getLogger(MicroServiceResponseHandler.class);

    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;

    @Autowired
    public MicroServiceResponseHandler(WebSocketSessionRegistry registry, ObjectMapper objectMapper) {
        super(Object.class);

        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleMessage(Object payload, Message originalMessage) {
        log.info("Received service message: <{}>", originalMessage);
        if (originalMessage.getReference() == null) {
            return;
        }
        List<String> referenceParts = new ArrayList<>(
            Arrays.asList(originalMessage.getReference().toString().split("\\."))
        );
        if (referenceParts.size() == 0) {
            return;
        }
        String sessionId = referenceParts.remove(0);
        if (!registry.has(sessionId)) {
            return;
        }
        WebSocketSession session = registry.get(sessionId);

        String reference = referenceParts.isEmpty() ? null : String.join(".", referenceParts);
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                WebSocketMessage.builder()
                    .type(originalMessage.getType())
                    .payload(payload)
                    .occurredAt(originalMessage.getOccurredAt())
                    .reference(reference)
                    .build()
            )));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}
