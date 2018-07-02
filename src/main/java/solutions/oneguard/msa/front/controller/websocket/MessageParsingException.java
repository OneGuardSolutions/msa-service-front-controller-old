/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
