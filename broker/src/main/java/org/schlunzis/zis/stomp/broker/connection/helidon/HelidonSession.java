package org.schlunzis.zis.stomp.broker.connection.helidon;

import io.helidon.websocket.WsCloseCodes;
import io.helidon.websocket.WsSession;
import org.schlunzis.zis.stomp.broker.connection.StompSession;

public record HelidonSession(WsSession session) implements StompSession {

    @Override
    public void send(String message) {
        session.send(message, true);
    }

    @Override
    public void close() {
        session.close(WsCloseCodes.NORMAL_CLOSE, "");
    }

}
