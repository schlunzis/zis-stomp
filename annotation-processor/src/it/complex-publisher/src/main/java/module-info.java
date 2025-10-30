module org.schlunzis.zis.stomp.client.it {
    requires org.schlunzis.zis.stomp.client;
    requires com.fasterxml.jackson.databind;
    requires jakarta.websocket.client;
    requires org.glassfish.tyrus.client;
    requires org.glassfish.tyrus.container.jdk.client;

    exports org.schlunzis.zis.stomp.client.it;
}
