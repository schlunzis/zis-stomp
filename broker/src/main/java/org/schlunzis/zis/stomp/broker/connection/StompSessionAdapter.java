package org.schlunzis.zis.stomp.broker.connection;

/// Adapter for session to allow the [org.schlunzis.zis.stomp.broker.StompBroker] to execute some methods on sessions
/// regardless which framework is used.
///
/// @param <SESSION> the type of sessions used by the broker
public interface StompSessionAdapter<SESSION> {

    /// Must send the given string as a text message over the websocket connection.
    ///
    /// It is assumed, that the message has been sent completely when this call returns.
    ///
    /// @param message the message to send
    /// @param session the session to send the message to
    /// @since 1.0.0
    void send(SESSION session, String message);

    /// Must close the connection as seen fit by the implementation.
    ///
    /// After being closed, no further interactions from this session should be passed to the broker.
    ///
    /// @param session the session to close
    /// @since 1.0.0
    void close(SESSION session);

    /// Can be used to override the equals semantics of sessions.
    ///
    /// Some frameworks provide different session instances in different states of connection or even for each message
    /// received. In that case this method has to be implemented to preserve equals semantics for the broker.
    ///
    /// @param a the first session
    /// @param b the second session
    /// @return `true` if the session correspond to the same connection. `false` otherwise.
    /// @since 1.0.0
    default boolean equals(SESSION a, SESSION b) {
        return a.equals(b);
    }

    /// Can be used to override the `hashCode` method of sessions.
    ///
    /// @param session the session to calculate the hash for
    /// @return the hash
    /// @see StompSessionAdapter#equals(SESSION, SESSION)
    /// @since 1.0.0
    default int hashCode(SESSION session) {
        return session.hashCode();
    }

}
