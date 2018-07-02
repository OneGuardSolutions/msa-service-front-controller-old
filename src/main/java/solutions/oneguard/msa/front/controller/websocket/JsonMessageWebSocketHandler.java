/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
import java.util.Collections;

import static solutions.oneguard.msa.front.controller.MicroServiceResponseHandler.WEB_SOCKET_SESSION_CONTEXT_KEY;

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
        Message<Object> serviceMessage = convert(wsm, session);

        producer.sendToService(targetService, serviceMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: {} <{}>", session.getId(), status);
        registry.unregister(session);
    }

    private Message<Object> convert(WebSocketMessage wsm, WebSocketSession session) {
        return Message.builder()
            .id(wsm.getId())
            .type(wsm.getType())
            .principal(session.getPrincipal().getName())
            .issuer(instance)
            .payload(wsm.getPayload())
            .context(Collections.singletonMap(WEB_SOCKET_SESSION_CONTEXT_KEY, session.getId()))
            .occurredAt(wsm.getOccurredAt())
            .respondToInstance(true)
            .build();
    }
}
