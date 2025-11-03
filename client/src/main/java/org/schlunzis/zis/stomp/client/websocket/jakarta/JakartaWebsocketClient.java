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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class JakartaWebsocketClient extends Endpoint implements WebSocketClient {

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
    public void connect(Map<String, List<String>> connectHeaders) throws ConnectionException {
        try {
            log.debug("Connecting to endpoint: {}", endpoint);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ConnectHeadersConfigurator(connectHeaders))
                    .encoders(List.of(JakartaFrameEncoder.class))
                    .decoders(List.of(JakartaFrameDecoder.class))
                    .build();
            container.connectToServer(this, config, endpoint);
            log.debug("Connected to endpoint: {}", endpoint);
        } catch (DeploymentException | IOException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionException(e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(Frame.class, this::onMessage);
    }

    private void onMessage(Frame frame) {
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
        log.debug("WebSocket connection closed");
    }

}
