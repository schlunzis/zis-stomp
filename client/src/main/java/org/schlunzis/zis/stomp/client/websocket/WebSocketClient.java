package org.schlunzis.zis.stomp.client.websocket;

import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.List;
import java.util.Map;

public interface WebSocketClient
        extends AutoCloseable {

    void connect(Map<String, List<String>> connectHeaders) throws ConnectionException;

    void send(Frame frame) throws SendException;

    void close();

}
