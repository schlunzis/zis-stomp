package org.schlunzis.zis.stomp.broker.connection.helidon;

import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;
import org.schlunzis.zis.stomp.broker.StompBroker;

import java.io.StringReader;

/// A [WsListener] implementation that passes all events to the given [StompBroker].
///
/// @since 1.0.0
public class HelidonWebsocketListener implements WsListener {

    private final StompBroker<WsSession> broker;

    /// Creates a listener that passes all events to the given [StompBroker].
    ///
    /// @param broker the broker to pass events to
    /// @since 1.0.0
    public HelidonWebsocketListener(StompBroker<WsSession> broker) {
        this.broker = broker;
    }

    @Override
    public void onOpen(WsSession session) {
        broker.onOpen(session);
    }

    @Override
    public void onMessage(WsSession session, String text, boolean last) {
        broker.onMessage(session, new StringReader(text));
    }

    @Override
    public void onClose(WsSession session, int status, String reason) {
        broker.onClose(session);
    }

    @Override
    public void onError(WsSession session, Throwable t) {
        broker.onError(session, t);
    }

}
