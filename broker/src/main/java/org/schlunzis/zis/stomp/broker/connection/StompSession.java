package org.schlunzis.zis.stomp.broker.connection;

/// Interface for WebsocketSessions that has to be implemented by all sessions given to the broker.
///
/// @since 1.0.0
public interface StompSession {

    /// Must send the given string as a text message over the websocket connection.
    ///
    /// It is assumed, that the message has been sent completely when this call returns.
    ///
    /// @param message the message to send
    /// @since 1.0.0
    void send(String message);

    /// Must close the connection as seen fit by the implementation.
    ///
    /// After being closed, no further interactions from this session should be passed to the broker.
    ///
    /// @since 1.0.0
    void close();

}
