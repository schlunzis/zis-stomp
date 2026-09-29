package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.StompBrokerBuilder;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketServerFactory;

public class StompBrokerFactory {

    public StompBroker create(StompBrokerBuilder builder) {
        WebsocketServerFactory serverFactory = builder.websocketServerFactory();
        if (serverFactory == null) {
            throw new IllegalStateException("WebsocketServerFactory is not set");
        }
        return new StompBrokerImpl(serverFactory);
    }

}
