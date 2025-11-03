package org.schlunzis.zis.stomp.client.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.*;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.Frames;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class Stomp1dot2Client implements StompClient {

    private static final Logger log = LoggerFactory.getLogger(Stomp1dot2Client.class);
    private static final String STRING_CONTENT_TYPE = "text/plain;charset=UTF-8";

    private final URI endpoint;
    private final WebSocketClient websocketClient;
    private final MessageConverter messageConverter;
    private final SubscriptionManager subscriptionManager;
    private final ReceiptManager receiptManager;

    @Nullable
    private final OnErrorConsumer onErrorConsumer;

    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.UNUSED);
    private final TransferQueue<Frame> connectedFrames = new LinkedTransferQueue<>();
    private final Lock mutex = new ReentrantLock();

    public Stomp1dot2Client(URI endpoint, MessageConverter messageConverter, @Nullable OnErrorConsumer onErrorConsumer,
                            Duration receiptTimeout, ReceiptPolicy receiptPolicy
    ) {
        this.endpoint = endpoint;
        this.websocketClient = new JakartaWebsocketClient(endpoint, this::handle);
        this.messageConverter = messageConverter;
        this.onErrorConsumer = onErrorConsumer;
        this.receiptManager = new ReceiptManager(websocketClient, receiptTimeout, receiptPolicy);
        this.subscriptionManager = new SubscriptionManager(messageConverter);
    }

    // ########
    // CONNECT
    // ########

    @Override
    public CompletableFuture<Void> connect() throws ConnectionException {
        Frame connectFrame = Frames.connect(endpoint);
        return CompletableFuture.runAsync(() -> doConnect(connectFrame, Map.of()));
    }

    @Override
    public CompletableFuture<Void> connect(String login, String passcode) throws ConnectionException {
        return connect(login, passcode, AuthenticationMethod.STOMP);
    }

    @Override
    public CompletableFuture<Void> connect(String login, String passcode, AuthenticationMethod authenticationMethod) throws ConnectionException {
        return switch (authenticationMethod) {
            case STOMP -> {
                Frame connectFrame = Frames.connect(endpoint, login, passcode);
                yield CompletableFuture.runAsync(() -> doConnect(connectFrame, Map.of()));
            }
            case HTTP_BASIC -> {
                String credentials = login + ":" + passcode;
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                String authHeaderValue = "Basic " + encodedCredentials;
                Frame connectFrame = Frames.connect(endpoint);
                yield CompletableFuture.runAsync(() -> doConnect(connectFrame, Map.of("Authorization", List.of(authHeaderValue))));
            }
        };
    }

    private void doConnect(Frame connectFrame, Map<String, List<String>> connectHeaders) throws ConnectionException {
        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.UNUSED) {
                throw new IllegalStateException("Client has already connected before. Current state: " + connectionState.get());
            }
            connectionState.set(ConnectionState.CONNECTING);

            websocketClient.connect(connectHeaders);
            websocketClient.send(connectFrame);

            Frame connectedFrame = connectedFrames.take();
            postProcessConnectedFrame(connectedFrame);
            connectionState.set(ConnectionState.CONNECTED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            doClose();
            throw new ConnectionException(e);
        } finally {
            mutex.unlock();
        }
    }

    private void postProcessConnectedFrame(Frame connectedFrame) {
        Headers headers = connectedFrame.headers();
        String version = headers.getFirst("version");
        if (version == null || !version.equals("1.2")) {
            doClose();
            throw new ConnectionException("Unsupported STOMP version: " + version);
        }
    }

    // #####
    // SEND
    // #####

    @Override
    public void send(String destination, String body) {
        ensureConnected();
        doSend(destination, body, STRING_CONTENT_TYPE);
    }

    @Override
    public void send(String destination, Object body) {
        ensureConnected();
        String convertedBody = messageConverter.convertToString(body);
        doSend(destination, convertedBody, messageConverter.contentType());
    }

    private void doSend(String destination, String body, String contentType) {
        Frame sendFrame = Frames.send(destination, body, contentType);
        receiptManager.sendAndAwaitReceiptIfPolicy(sendFrame, ReceiptPolicy.Policy.FOR_SEND);
    }

    // ##########
    // SUBSCRIBE
    // ##########

    @Override
    public <T> Subscription subscribe(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        ensureConnected();

        Subscription subscription = subscriptionManager.create(
                destination,
                new SubscriberInvoker(messageConverter, payloadType, messageHandler)
        );
        doSubscribe(subscription);
        return subscription;
    }

    @Override
    public void subscribe(Object subscriber) {
        mutex.lock();
        try {
            ensureConnected();
            if (subscriptionManager.hasSubscriptionsForSubscriber(subscriber)) {
                throw new IllegalStateException("Subscriber is already subscribed: " + subscriber);
            }

            Set<StompSubscription> subscriptions = subscriptionManager.createAnnotatedSubscriptions(subscriber);
            subscriptions.forEach(this::doSubscribe);
        } finally {
            mutex.unlock();
        }
    }

    private void doSubscribe(Subscription subscription) {
        Frame subscribeFrame = Frames.subscribe(subscription.destination(), subscription.id().toString(), "auto");
        receiptManager.sendAndAwaitReceiptIfPolicy(subscribeFrame, ReceiptPolicy.Policy.FOR_SUBSCRIBE);
    }

    // ############
    // UNSUBSCRIBE
    // ############

    @Override
    public void unsubscribe(Subscription subscription) {
        ensureConnected();

        if (!subscriptionManager.contains(subscription.id())) {
            return;
        }
        doUnsubscribe(subscription);
        subscriptionManager.remove(subscription);
    }

    @Override
    public void unsubscribe(Object subscriber) {
        mutex.lock();
        try {
            ensureConnected();
            Set<StompSubscription> subscriptions = subscriptionManager.remove(subscriber);
            if (subscriptions == null || subscriptions.isEmpty()) {
                return;
            }
            subscriptions.forEach(this::doUnsubscribe);
        } finally {
            mutex.unlock();
        }
    }

    private void doUnsubscribe(Subscription subscription) {
        Frame unsubscribeFrame = Frames.unsubscribe(subscription.id().toString());
        receiptManager.sendAndAwaitReceiptIfPolicy(unsubscribeFrame, ReceiptPolicy.Policy.FOR_UNSUBSCRIBE);
    }

    // ###########
    // DISCONNECT
    // ###########

    @Override
    public void close() {
        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.CONNECTED) {
                return;
            }
            connectionState.set(ConnectionState.DISCONNECTING);

            Frame disconnectFrame = Frames.disconnect();
            receiptManager.sendAndAwaitReceiptIfPolicy(disconnectFrame, ReceiptPolicy.Policy.FOR_DISCONNECT);
            doClose();
        } finally {
            mutex.unlock();
        }
    }

    private void doClose() {
        mutex.lock();
        try {
            connectionState.set(ConnectionState.DISCONNECTING);
            websocketClient.close();
            connectionState.set(ConnectionState.DISCONNECTED);
            receiptManager.clear();
            subscriptionManager.clear();
        } finally {
            mutex.unlock();
        }
    }

    // ##############
    // OTHER METHODS
    // ##############

    @Override
    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    /// If the client is not connected, this method throws an [IllegalStateException].
    /// This method is used to ensure that operations that require a connected client
    /// are only performed when the client is indeed connected.
    ///
    /// @throws IllegalStateException if the client is not connected
    private void ensureConnected() {
        if (connectionState.get() != ConnectionState.CONNECTED) {
            throw new IllegalStateException("Client is not connected");
        }
    }

    /// This is the entry point for incoming STOMP frames from the WebSocket.
    ///
    /// @param frame the received STOMP frame
    private void handle(Frame frame) {
        switch (frame.command()) {
            case CONNECTED -> {
                if (connectionState.get() != ConnectionState.CONNECTING) {
                    log.warn("Received CONNECTED frame while not connecting: {}", frame);
                    return;
                }
                connectedFrames.add(frame);
            }
            case MESSAGE -> subscriptionManager.handleMessage(frame);
            case RECEIPT -> receiptManager.handleReceipt(frame);
            case ERROR -> {
                if (onErrorConsumer != null) {
                    String message = frame.headers().getFirst("message");
                    onErrorConsumer.accept(message != null ? message : "Unknown Error", frame);
                } else {
                    log.error("Received STOMP ERROR frame: {}", frame);
                }
                doClose();
            }
            case CONNECT, STOMP, SEND, SUBSCRIBE, UNSUBSCRIBE, ACK, NACK, BEGIN, COMMIT, ABORT, DISCONNECT ->
                    log.warn("Received frame with client command: {}", frame.command());
        }
    }

}
