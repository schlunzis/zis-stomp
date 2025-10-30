package org.schlunzis.zis.stomp.client.protocol;

import java.net.URI;

/**
 * Utility class for creating STOMP frames.
 *
 * @since 1.0.0
 */
public final class Frames {

    private Frames() {
    }

    public static Frame connect(URI endpoint) {
        final String host = endpoint.getHost();
        return Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", host)
                .build();
    }

    public static Frame send(String destination, String body, String contentType) {
        return Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", contentType)
                .body(body)
                .build();
    }

    public static Frame subscribe(String destination, String id, String ackMode) {
        return Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", destination)
                .header("id", id)
                .header("ack", ackMode)
                .build();
    }

    public static Frame unsubscribe(String id) {
        return Frame.builder()
                .command(Command.UNSUBSCRIBE)
                .header("id", id)
                .build();
    }

    public static Frame disconnect() {
        return Frame.builder()
                .command(Command.DISCONNECT)
                .build();
    }

}
