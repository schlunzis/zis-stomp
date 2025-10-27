package org.schlunzis.zis.stomp.client.websocket;

import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;

public sealed interface WebSocketClient
        extends AutoCloseable
        permits JakartaWebsocketClient {

    void connect() throws ConnectionException;

    void send(Frame frame) throws SendException;

    void close();

}
