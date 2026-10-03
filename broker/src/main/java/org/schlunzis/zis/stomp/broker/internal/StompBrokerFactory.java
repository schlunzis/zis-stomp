package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.StompBrokerBuilder;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

/// Factory for the broker
/// @param <SESSION> the type of session handled by the broker
public class StompBrokerFactory<SESSION extends WebsocketSession> {

    /// Creates a broker from the given builder.
    public StompBroker<SESSION> create(StompBrokerBuilder<SESSION> builder) {
        return new StompBrokerImpl<>();
    }

}
