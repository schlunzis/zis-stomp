package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

import java.io.Reader;

/// A stomp broker
///
/// @param <SESSION> the type of session handled by this broker
public interface StompBroker<SESSION extends WebsocketSession> extends AutoCloseable {


    /// Creates a new [StompBrokerBuilder] for building a STOMP client.
    ///
    /// Builder instances are not thread-safe and should not be shared between threads.
    ///
    /// @param <SESSION> the type of session handled by the broker
    /// @return a new instance of [StompBrokerBuilder]
    /// @since 1.0.0
    static <SESSION extends WebsocketSession> StompBrokerBuilder<SESSION> builder() {
        return new StompBrokerBuilder<>();
    }

    /// Starts processing of received frames.
    void start();

    /// To be called when a new connection is opened.
    ///
    /// @param session the new session for the connection
    void onOpen(SESSION session);

    /// To be called when a new message has been received.
    ///
    /// @param session the session the message was received from
    /// @param message the message received
    void onMessage(SESSION session, Reader message);

    /// To be called when an error occurred for the session.
    ///
    /// @param session the session with the error
    /// @param t       a throwable with more information if available. May be `null`
    void onError(SESSION session, @Nullable Throwable t);

    /// To be called when the connection to the client has been closed by the client.
    ///
    /// @param session the session that has been closed
    void onClose(SESSION session);

    /// Stops processing of received frames.
    void close();

}
