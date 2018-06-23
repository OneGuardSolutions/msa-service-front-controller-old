package solutions.oneguard.msa.front.controller.websocket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

class MessageParser {
    private static final Logger log = LoggerFactory.getLogger(MessageParser.class);

    private final ObjectMapper objectMapper;
    private final ObjectReader reader;

    MessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        reader = objectMapper.readerFor(WebSocketMessage.class);
    }

    WebSocketMessage parseMessage(TextMessage message) throws IOException, MessageParsingException {
        try {
            return reader.readValue(message.getPayload());
        } catch (JsonParseException e) {
            log.error("Received WebSocket message with invalid format", e);
            throw new MessageParsingException(buildInvalidFormatMessage(message.getPayload()));
        } catch (JsonMappingException e) {
            log.error("Received WebSocket message with malformed message", e);
            throw new MessageParsingException(buildMalformedMessageResponse(message.getPayload()));
        }
    }

    private TextMessage buildInvalidFormatMessage(String originalPayload) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(
                WebSocketMessage.builder()
                    .type("error.invalid_format")
                    .payload(new InvalidFormatPayload(Collections.singletonList("json"), originalPayload))
                    .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private TextMessage buildMalformedMessageResponse(String originalPayload) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(
                WebSocketMessage.builder()
                    .type("error.malformed_message")
                    .payload(new MalformedMessagePayload(
                        Arrays.asList("type", "payload", "occurredAt", "reference"),
                        originalPayload)
                    )
                    .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    private static class InvalidFormatPayload {
        private Collection<String> supportedFormats;
        private String originalContent;
    }

    @Data
    @AllArgsConstructor
    private static class MalformedMessagePayload {
        private Collection<String> allowedProperties;
        private String originalContent;
    }
}
