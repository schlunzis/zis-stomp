package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.Headers;
import org.schlunzis.zis.stomp.client.subscriptions.SubscriberSubscriptionFactory;
import org.schlunzis.zis.stomp.client.subscriptions.SubscriptionManager;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

final class Stomp1dot2Client implements StompClient {

    private static final Logger log = LoggerFactory.getLogger(Stomp1dot2Client.class);
    private static final String STRING_CONTENT_TYPE = "text/plain;charset=UTF-8";

    private final URI endpoint;
    private final WebSocketClient websocketClient;
    private final SubscriptionManager subscriptionManager = new SubscriptionManager();
    private final Collection<Subscription> subscriberSubscriptions;
    private final MessageConverter messageConverter;

    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.UNUSED);
    private final TransferQueue<Frame> connectedFrames = new LinkedTransferQueue<>();
    private final Lock mutex = new ReentrantLock();

    Stomp1dot2Client(URI endpoint, List<Object> subscribers, MessageConverter messageConverter) {
        this.endpoint = endpoint;
        SubscriberSubscriptionFactory subscriberSubscriptionFactory = new SubscriberSubscriptionFactory(messageConverter);
        this.subscriberSubscriptions = subscriberSubscriptionFactory.createAll(subscribers, this.subscriptionManager);
        this.websocketClient = new JakartaWebsocketClient(endpoint, this::handle);
        this.messageConverter = messageConverter;
    }

    @Override
    public void connect() throws ConnectionException {
        final String host = endpoint.getHost();

        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.UNUSED) {
                throw new IllegalStateException("Client has already connected before. Current state: " + connectionState.get());
            }
            connectionState.set(ConnectionState.CONNECTING);

            websocketClient.connect();
            Frame connectFrame = Frame.builder()
                    .command(Command.CONNECT)
                    .header("accept-version", "1.2")
                    .header("host", host)
                    .build();
            websocketClient.send(connectFrame);
            Frame connectedFrame = connectedFrames.take();
            Headers headers = connectedFrame.headers();
            String version = headers.getFirst("version");
            if (version == null || !version.startsWith("1.2")) {
                websocketClient.close();
                connectionState.set(ConnectionState.DISCONNECTED);
                throw new ConnectionException("Unsupported STOMP version: " + version);
            }
            connectionState.set(ConnectionState.CONNECTED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException(e);
        } finally {
            mutex.unlock();
        }

        subscriberSubscriptions.forEach(this::doSubscribe);
    }

    @Override
    public void send(String destination, String body) {
        ensureConnected();
        send(destination, body, STRING_CONTENT_TYPE);
    }

    @Override
    public void send(String destination, Object body) {
        ensureConnected();
        String convertedBody = messageConverter.convertToString(body);
        send(destination, convertedBody, messageConverter.contentType());
    }

    private void send(String destination, String body, String contentType) {
        Frame sendFrame = Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", contentType)
                .body(body)
                .build();
        websocketClient.send(sendFrame);
    }

    @Override
    public <T> Subscription subscribe(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        ensureConnected();

        Subscription subscription = subscriptionManager.create(
                destination,
                message -> {
                    try {
                        if (payloadType.equals(String.class)) {
                            //noinspection unchecked
                            messageHandler.accept((T) message);
                        } else {
                            log.debug("Trying to convert message to type {}: {}", payloadType, message);
                            T convertedMessage = messageConverter.convertToType(message, payloadType);
                            messageHandler.accept(convertedMessage);
                        }
                    } catch (ConversionException e) {
                        log.error("Error converting message to type {}: {}", payloadType, e.getMessage(), e);
                    }
                }
        );
        doSubscribe(subscription);
        return subscription;
    }

    private void doSubscribe(Subscription subscription) {
        Frame subscribeFrame = Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", subscription.destination())
                .header("id", subscription.id().toString())
                .header("ack", "auto") // TODO make ack mode configurable
                .build();

        websocketClient.send(subscribeFrame);
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        ensureConnected();

        if (!subscriptionManager.contains(subscription.id())) {
            return;
        }
        doUnsubscribe(subscription);
        subscriptionManager.remove(subscription);
    }

    private void doUnsubscribe(Subscription subscription) {
        Frame unsubscribeFrame = Frame.builder()
                .command(Command.UNSUBSCRIBE)
                .header("id", subscription.id().toString())
                .build();

        websocketClient.send(unsubscribeFrame);
    }

    @Override
    public void close() {
        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.CONNECTED) {
                return;
            }
            connectionState.set(ConnectionState.DISCONNECTING);

            Frame disconnectFrame = Frame.builder()
                    .command(Command.DISCONNECT)
                    .build();
            websocketClient.send(disconnectFrame); // TODO use receipt
            websocketClient.close();
            connectionState.set(ConnectionState.DISCONNECTED);
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    private void ensureConnected() {
        if (connectionState.get() != ConnectionState.CONNECTED) {
            throw new IllegalStateException("Client is not connected");
        }
    }

    private void handle(Frame frame) {
        switch (frame.command()) {
            case CONNECTED -> connectedFrames.add(frame);
            case MESSAGE -> {
                List<String> subscriptionId = frame.headers().get("subscription");
                if (subscriptionId != null && !subscriptionId.isEmpty()) {
                    subscriptionManager.handleMessage(
                            UUID.fromString(subscriptionId.get(0)),
                            frame.body()
                    );
                } else {
                    log.warn("Received MESSAGE without subscription id: {}", frame);
                }
            }
            case RECEIPT -> {
                log.debug("Received: {}", frame);
            }
            case ERROR -> {
                log.error("Received ERROR frame: {}", frame);
            }
            case CONNECT, STOMP, SEND, SUBSCRIBE, UNSUBSCRIBE, ACK, NACK, BEGIN, COMMIT, ABORT, DISCONNECT ->
                    log.warn("Received frame with client command: {}", frame.command());
        }
    }

    private enum ConnectionState {
        UNUSED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

}
