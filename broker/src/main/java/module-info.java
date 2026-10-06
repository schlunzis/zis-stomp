import org.jspecify.annotations.NullMarked;

/// This module provides a STOMP broker implementation.
///
/// @see org.schlunzis.zis.stomp.broker.StompBroker
/// @since 1.0.0
@NullMarked
module org.schlunzis.zis.stomp.broker {
    requires org.schlunzis.zis.stomp.common;
    requires org.slf4j;
    requires org.jspecify;

    requires static io.avaje.jex.websocket;
    requires static io.helidon.websocket;

    exports org.schlunzis.zis.stomp.broker;
    exports org.schlunzis.zis.stomp.broker.connection;
    exports org.schlunzis.zis.stomp.broker.connection.jax;
    exports org.schlunzis.zis.stomp.broker.connection.helidon;
}
