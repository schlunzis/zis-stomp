package org.schlunzis.zis.stomp.broker.websocket;

/// Interface for WebsocketSessions that has to be implemented by all sessions given to the broker.
public interface WebsocketSession {

    /// Must send the given string as a text message over the websocket connection.
    ///
    /// @param message the message to send
    void send(String message);

    /// Must close the connection as seen fit by the implementation.
    /// After being closed, no further interactions from this session should be passed to the broker.
    void close();

}
