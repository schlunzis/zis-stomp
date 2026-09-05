package org.schlunzis.zis.stomp.common.protocol;

import java.net.URI;

/// Utility class for creating STOMP frames.
///
/// @since 1.0.0
public final class Frames {

    private Frames() {
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: CONNECT
    /// - Header: accept-version: 1.2
    /// - Header: host: the host from the given URI
    ///
    /// @param endpoint the endpoint that the constructed frame should be sent to.
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder connect(URI endpoint) {
        final String host = endpoint.getHost();
        return Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", host);
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: CONNECT
    /// - Header: accept-version: 1.2
    /// - Header: host: the host from the given URI
    /// - Header: login: the given login
    /// - Header: passcode: the given passcode
    ///
    /// @param endpoint the endpoint that the constructed frame should be sent to.
    /// @param login    the login (e.g. username)
    /// @param passcode the passcode to authenticate
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder connect(URI endpoint, String login, String passcode) {
        final String host = endpoint.getHost();
        return Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", host)
                .header("login", login)
                .header("passcode", passcode);
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: SEND
    /// - Header: destination: the given destination
    /// - Header: content-type: the given content-type
    /// - Body: the given Body
    ///
    /// @param destination the destination topic to send to
    /// @param body        the body of the frame
    /// @param contentType the content type of the body
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder send(String destination, String body, String contentType) {
        return Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", contentType)
                .body(body);
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: SUBSCRIBE
    /// - Header: destination: the given destination
    /// - Header: id: the given id for the subscription
    /// - Header: ackMode: the ack mode for the subscription
    ///
    /// @param destination the destination topic to send to
    /// @param id          the id for the subscription
    /// @param ackMode     the ack mode for the subscription
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder subscribe(String destination, String id, String ackMode) {
        return Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", destination)
                .header("id", id)
                .header("ack", ackMode);
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: UNSUBSCRIBE
    /// - Header: id: the given id for the subscription
    ///
    /// @param id the id of the subscription
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder unsubscribe(String id) {
        return Frame.builder()
                .command(Command.UNSUBSCRIBE)
                .header("id", id);
    }

    /// Creates a FrameBuilder with the following preset values:
    ///
    /// - Command: DISCONNECT
    ///
    /// @return a FrameBuilder with the preset values.
    public static FrameBuilder disconnect() {
        return Frame.builder()
                .command(Command.DISCONNECT);
    }

}
