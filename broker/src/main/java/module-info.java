import org.jspecify.annotations.NullMarked;

/// This module provides a STOMP broker implementation using Jakarta WebSocket API.
///
/// @since 1.0.0
@NullMarked
module org.schlunzis.zis.stomp.broker {
    requires org.schlunzis.zis.stomp.common;
    requires jakarta.websocket;
    requires org.slf4j;
    requires org.jspecify;

    requires static com.fasterxml.jackson.databind;
    requires static tools.jackson.databind;
    uses com.fasterxml.jackson.core.ObjectCodec;
    uses tools.jackson.databind.ObjectMapper;

    // Has to be open to everything, because different jakarta websocket implementations
    // use reflection to access the endpoint classes and have different module names
    opens org.schlunzis.zis.stomp.broker.websocket.jakarta;

    exports org.schlunzis.zis.stomp.broker;
}
