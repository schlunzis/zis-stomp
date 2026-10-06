package org.schlunzis.zis.stomp.broker.connection.helidon;

import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;
import org.schlunzis.zis.stomp.broker.StompBroker;

import java.io.StringReader;

public class HelidonWebsocketListener implements WsListener {

    private final StompBroker<HelidonSession> broker;

    public HelidonWebsocketListener(StompBroker<HelidonSession> broker) {
        this.broker = broker;
    }

    @Override
    public void onOpen(WsSession session) {
        broker.onOpen(new HelidonSession(session));
    }

    @Override
    public void onMessage(WsSession session, String text, boolean last) {
        broker.onMessage(new HelidonSession(session), new StringReader(text));
    }

    @Override
    public void onClose(WsSession session, int status, String reason) {
        broker.onClose(new HelidonSession(session));
    }

    @Override
    public void onError(WsSession session, Throwable t) {
        broker.onError(new HelidonSession(session), t);
    }

}
