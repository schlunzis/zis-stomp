package org.schlunzis.zis.stomp.client.websocket;

import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;

import java.util.List;
import java.util.Map;

public sealed interface WebSocketClient
        extends AutoCloseable
        permits JakartaWebsocketClient {

    void connect(Map<String, List<String>> connectHeaders) throws ConnectionException;

    void send(Frame frame) throws SendException;

    void close();

}
