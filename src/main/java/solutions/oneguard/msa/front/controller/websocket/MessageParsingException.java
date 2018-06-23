package solutions.oneguard.msa.front.controller.websocket;

import org.springframework.web.socket.TextMessage;

class MessageParsingException extends Exception {
    private final TextMessage response;

    MessageParsingException(TextMessage response) {
        this.response = response;
    }

    TextMessage getResponse() {
        return response;
    }
}
