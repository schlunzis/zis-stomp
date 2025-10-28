module org.schlunzis.zis.stomp.client.it {
    requires org.schlunzis.zis.stomp.client;
    requires tools.jackson.databind;
    requires jakarta.websocket.client;
    requires org.glassfish.tyrus.client;
    requires org.glassfish.tyrus.container.jdk.client;

    exports org.schlunzis.zis.stomp.client.it;
}
