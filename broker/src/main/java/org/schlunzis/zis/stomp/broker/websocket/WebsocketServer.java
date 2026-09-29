package org.schlunzis.zis.stomp.broker.websocket;

public interface WebsocketServer extends AutoCloseable {

    void send(String frame);

    void close();

}
