package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.*;
import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint(
        encoders = {
                JakartaFrameEncoder.class
        },
        decoders = {
                JakartaFrameDecoder.class
        }
)
public final class JakartaWebsocketClient implements WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(JakartaWebsocketClient.class);

    private final URI endpoint;
    private final Consumer<Frame> frameHandler;

    @Nullable
    private Session session;

    public JakartaWebsocketClient(URI endpoint, Consumer<Frame> frameHandler) {
        this.endpoint = endpoint;
        this.frameHandler = frameHandler;
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
    public void onMessage(Frame frame, Session session) {
        log.debug("Received frame: {}", frame);
        frameHandler.accept(frame);
    }

    @Override
    public void send(Frame frame) throws SendException {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send frame, client is not connected");
            throw new SendException("Cannot send frame");
        }

        try {
            log.debug("Sending frame: {}", frame);
            session.getBasicRemote().sendObject(frame);
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
