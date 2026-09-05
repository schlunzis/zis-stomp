import org.jspecify.annotations.NullMarked;

/// Common classes for the ZIS STOMP client and broker.
@NullMarked
module org.schlunzis.zis.stomp.common {
    requires org.jspecify;

    exports org.schlunzis.zis.stomp.common;
    exports org.schlunzis.zis.stomp.common.protocol to org.schlunzis.zis.stomp.client;
}
