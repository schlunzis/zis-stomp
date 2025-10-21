import org.jspecify.annotations.NullMarked;

/**
 * This module provides a STOMP client implementation using Jakarta WebSocket API.
 * <p>
 * It includes support for subscribing to STOMP topics and publishing messages,
 * with optional integration for JSON message conversion using Jackson 2 or Jackson 3.
 */
@NullMarked
module org.schlunzis.zis.stomp.client {
    requires jakarta.websocket.client;
    requires org.slf4j;
    requires org.jspecify;

    requires static com.fasterxml.jackson.databind;
    requires static tools.jackson.databind;
    uses com.fasterxml.jackson.core.ObjectCodec;
    uses tools.jackson.databind.ObjectMapper;

    opens org.schlunzis.zis.stomp.client.websocket.jakarta;

    exports org.schlunzis.zis.stomp.client;
}
