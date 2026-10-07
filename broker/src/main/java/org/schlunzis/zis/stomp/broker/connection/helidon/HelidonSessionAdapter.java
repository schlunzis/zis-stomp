package org.schlunzis.zis.stomp.broker.connection.helidon;

import io.helidon.websocket.WsCloseCodes;
import io.helidon.websocket.WsSession;
import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;

/// Simple adapter implementation for [WsSession].
///
/// @since 1.0.0
public class HelidonSessionAdapter implements StompSessionAdapter<WsSession> {

    /// Creates a new session adapter for helidon websocket sessions.
    ///
    /// @since 1.0.0
    public HelidonSessionAdapter() {
    }

    @Override
    public void send(WsSession session, String message) {
        session.send(message, true);
    }

    @Override
    public void close(WsSession session) {
        session.close(WsCloseCodes.NORMAL_CLOSE, "");
    }

}
