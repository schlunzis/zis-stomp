package org.schlunzis.zis.stomp.client.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.*;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.Frames;
import org.schlunzis.zis.stomp.client.subscriptions.SubscriberSubscriptionFactory;
import org.schlunzis.zis.stomp.client.subscriptions.SubscriptionManager;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final SubscriptionManager subscriptionManager = new SubscriptionManager();
    private final SubscriberSubscriptionFactory subscriberSubscriptionFactory;
    private final Map<Object, Set<Subscription>> subscriberSubscriptions = new ConcurrentHashMap<>();
    private final MessageConverter messageConverter;
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
        this.subscriberSubscriptionFactory = new SubscriberSubscriptionFactory(messageConverter);
    }

    @Override
    public void connect() throws ConnectionException {

        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.UNUSED) {
                throw new IllegalStateException("Client has already connected before. Current state: " + connectionState.get());
            }
            connectionState.set(ConnectionState.CONNECTING);

            websocketClient.connect();
            Frame connectFrame = Frames.connect(endpoint);
            websocketClient.send(connectFrame);
            Frame connectedFrame = connectedFrames.take();
            postProcessConnectedFrame(connectedFrame);
            connectionState.set(ConnectionState.CONNECTED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    @Override
    public void subscribe(Object subscriber) {
        mutex.lock();
        try {
            ensureConnected();
            if (subscriberSubscriptions.containsKey(subscriber)) {
                throw new IllegalStateException("Subscriber is already subscribed: " + subscriber);
            }

            subscriberSubscriptions.computeIfAbsent(
                    subscriber,
                    _ -> {
                        Set<Subscription> s = new HashSet<>();
                        subscriberSubscriptionFactory.createAll(subscriptionManager, subscriber).forEach(subscription -> {
                            doSubscribe(subscription);
                            s.add(subscription);
                        });
                        return s;
                    }
            );
        } finally {
            mutex.unlock();
        }
    }

    private void doSubscribe(Subscription subscription) {
        Frame subscribeFrame = Frames.subscribe(subscription.destination(), subscription.id().toString(), "auto");
        receiptManager.sendAndAwaitReceiptIfPolicy(subscribeFrame, ReceiptPolicy.Policy.FOR_SUBSCRIBE);
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

    @Override
    public void unsubscribe(Object subscriber) {
        mutex.lock();
        try {
            ensureConnected();
            Set<Subscription> subscriptions = subscriberSubscriptions.remove(subscriber);
            if (subscriptions == null || subscriptions.isEmpty()) {
                return;
            }
            for (Subscription subscription : subscriptions) {
                doUnsubscribe(subscription);
                subscriptionManager.remove(subscription);
            }
        } finally {
            mutex.unlock();
        }
    }

    private void doUnsubscribe(Subscription subscription) {
        Frame unsubscribeFrame = Frames.unsubscribe(subscription.id().toString());
        receiptManager.sendAndAwaitReceiptIfPolicy(unsubscribeFrame, ReceiptPolicy.Policy.FOR_UNSUBSCRIBE);
    }

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
            subscriberSubscriptions.clear();
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

    /**
     * This is the entry point for incoming STOMP frames from the WebSocket.
     *
     * @param frame the received STOMP frame
     */
    private void handle(Frame frame) {
        switch (frame.command()) {
            case CONNECTED -> connectedFrames.add(frame);
            case MESSAGE -> {
                List<String> subscriptionId = frame.headers().get("subscription");
                if (subscriptionId != null && !subscriptionId.isEmpty()) {
                    subscriptionManager.handleMessage(
                            UUID.fromString(subscriptionId.getFirst()),
                            frame.body().orElse("")
                    );
                } else {
                    log.warn("Received MESSAGE without subscription id: {}", frame);
                }
            }
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

    /**
     * Enum for the connection state of the client.
     */
    private enum ConnectionState {
        /**
         * Client has not been used yet.
         * It has not connected before.
         */
        UNUSED,
        /**
         * Client is in the process of connecting.
         */
        CONNECTING,
        /**
         * Client is connected and ready to use.
         * This is the only state where sending and subscribing is allowed.
         */
        CONNECTED,
        /**
         * Client is in the process of disconnecting.
         */
        DISCONNECTING,
        /**
         * Client is disconnected.
         * No further operations are allowed.
         */
        DISCONNECTED
    }

}
