package solutions.oneguard.msa.front.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import solutions.oneguard.msa.core.messaging.MessageProducer;
import solutions.oneguard.msa.core.model.Instance;
import solutions.oneguard.msa.core.model.Message;

import java.io.IOException;

@Component
public class JsonMessageWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(JsonMessageWebSocketHandler.class);

    private final MessageProducer producer;
    private final Instance instance;
    private final WebSocketSessionRegistry registry;
    private final MessageParser messageParser;

    @Autowired
    public JsonMessageWebSocketHandler(
        MessageProducer producer,
        Instance instance,
        WebSocketSessionRegistry registry,
        ObjectMapper objectMapper
    ) {
        this.producer = producer;
        this.instance = instance;
        this.registry = registry;
        messageParser = new MessageParser(objectMapper);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connection established: {} <{}>", session.getId(), session);
        registry.register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        WebSocketMessage wsm;
        try {
            wsm = messageParser.parseMessage(message);
        } catch (MessageParsingException e) {
            session.sendMessage(e.getResponse());
            return;
        }
        log.info("WebSocket message message received: <{}>", wsm);

        String type = wsm.getType();
        String targetService = type.substring(0, type.indexOf('.'));
        String reference = wsm.getReference() == null ? session.getId() : session.getId() + '.' + wsm.getReference();
        Message serviceMessage = Message.builder()
            .issuer(instance)
            .type(wsm.getType())
            .principal(session.getPrincipal().getName())
            .payload(wsm.getPayload())
            .occuredAt(wsm.getOccurredAt())
            .reference(reference)
            .respondToIssuer(true)
            .build();

        producer.sendToService(targetService, serviceMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: {} <{}>", session.getId(), status);
        registry.unregister(session);
    }
}
