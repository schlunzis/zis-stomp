import org.jspecify.annotations.NullMarked;

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
