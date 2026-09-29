package org.schlunzis.zis.stomp.broker;

public interface StompBroker extends AutoCloseable {


    /// Creates a new [StompBrokerBuilder] for building a STOMP client.
    ///
    /// The builder must be provided with a [org.schlunzis.zis.stomp.broker.websocket.WebsocketServer] before building the broker.
    ///
    /// Builder instances are not thread-safe and should not be shared between threads.
    ///
    /// @return a new instance of [StompBrokerBuilder]
    /// @since 1.0.0
    static StompBrokerBuilder builder() {
        return new StompBrokerBuilder();
    }

    /// Starts processing of received frames.
    void start();

    /// Stops processing of received frames.
    void close();

}
