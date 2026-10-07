package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.Authenticator;
import org.schlunzis.zis.stomp.broker.NoopAuthenticator;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.StompBrokerBuilder;
import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;

import java.util.Arrays;

/// Factory for the broker
///
/// @param <SESSION> the type of session handled by the broker
public class StompBrokerFactory<SESSION> {

    /// Creates a broker from the given builder.
    public StompBroker<SESSION> create(StompBrokerBuilder<SESSION> builder) {
        Authenticator authenticator = builder.authenticator();
        if (authenticator == null) {
            authenticator = new NoopAuthenticator();
        }
        StompSessionAdapter<SESSION> sessionAdapter = builder.sessionAdapter();
        if (sessionAdapter == null) {
            throw new IllegalStateException("No session adapter set");
        }
        return new StompBrokerImpl<>(
                Arrays.copyOf(builder.hosts(), builder.hosts().length),
                authenticator,
                sessionAdapter
        );
    }

}
