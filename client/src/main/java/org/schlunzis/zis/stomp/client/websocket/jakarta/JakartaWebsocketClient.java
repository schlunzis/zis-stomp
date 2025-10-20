package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.*;
import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Message;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint(
        encoders = {
                JakartaMessageEncoder.class
        },
        decoders = {
                JakartaMessageDecoder.class
        }
)
public final class JakartaWebsocketClient implements WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(JakartaWebsocketClient.class);

    private final URI endpoint;
    private final Consumer<Message> messageHandler;

    @Nullable
    private Session session;

    public JakartaWebsocketClient(URI endpoint, Consumer<Message> messageHandler) {
        this.endpoint = endpoint;
        this.messageHandler = messageHandler;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("Connecting to endpoint: {}", endpoint);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpoint);
            log.debug("Connected to endpoint: {}", endpoint);
        } catch (DeploymentException | IOException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected to {}", session.getRequestURI());
        this.session = session;
    }

    @OnMessage
    public void onMessage(Message message, Session session) {
        log.debug("Received message: {}", message);
        messageHandler.accept(message);
    }

    @Override
    public void send(Message message) throws SendException {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send message, client is not connected");
            return;
        }

        try {
            log.debug("Sending message: {}", message);
            session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            throw new SendException(e);
        }
    }

    @Override
    public void close() {
        if (session == null || !session.isOpen())
            return;

        try {
            session.close();
        } catch (IOException e) {
            log.error("Error closing WebSocket session", e);
        }
        log.info("WebSocket connection closed");
    }

}
