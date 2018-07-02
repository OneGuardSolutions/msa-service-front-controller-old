/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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

@Component
public class MicroServiceResponseHandler extends AbstractMessageHandler<Object> {
    public static final String WEB_SOCKET_SESSION_CONTEXT_KEY = "webSocketSession";

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
    public void handleMessage(Message<Object> originalMessage) {
        log.info("Received service message: <{}>", originalMessage);
        if (
            originalMessage.getContext() == null ||
            !originalMessage.getContext().containsKey(WEB_SOCKET_SESSION_CONTEXT_KEY)
        ) {
            return;
        }
        String sessionId = originalMessage.getContext().get(WEB_SOCKET_SESSION_CONTEXT_KEY).toString();
        WebSocketSession session = registry.get(sessionId);
        if (session == null) {
            return;
        }

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                WebSocketMessage.builder()
                    .id(originalMessage.getResponseTo())
                    .type(originalMessage.getType())
                    .payload(originalMessage.getPayload())
                    .occurredAt(originalMessage.getOccurredAt())
                    .build()
            )));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}
