/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package solutions.oneguard.msa.front.controller.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void unregister(WebSocketSession session) {
        unregister(session.getId());
    }

    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    public boolean has(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public WebSocketSession get(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(String.format("no session with id '%s' found", sessionId));
        }

        return session;
    }
}
