package org.schlunzis.zis.stomp.client.protocol;

import java.net.URI;

/// Utility class for creating STOMP frames.
///
/// @since 1.0.0
public final class Frames {

    private Frames() {
    }

    public static FrameBuilder connect(URI endpoint) {
        final String host = endpoint.getHost();
        return Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", host);
    }

    public static FrameBuilder connect(URI endpoint, String login, String passcode) {
        final String host = endpoint.getHost();
        return Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", host)
                .header("login", login)
                .header("passcode", passcode);
    }

    public static FrameBuilder send(String destination, String body, String contentType) {
        return Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", contentType)
                .body(body);
    }

    public static FrameBuilder subscribe(String destination, String id, String ackMode) {
        return Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", destination)
                .header("id", id)
                .header("ack", ackMode);
    }

    public static FrameBuilder unsubscribe(String id) {
        return Frame.builder()
                .command(Command.UNSUBSCRIBE)
                .header("id", id);
    }

    public static FrameBuilder disconnect() {
        return Frame.builder()
                .command(Command.DISCONNECT);
    }

}
