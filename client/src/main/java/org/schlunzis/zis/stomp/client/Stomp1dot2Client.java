package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
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
import java.util.concurrent.CountDownLatch;
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
    private final SubscriberSubscriptionFactory subscriberSubscriptionFactory;
    private final Map<Object, Set<Subscription>> subscriberSubscriptions = new ConcurrentHashMap<>();
    private final MessageConverter messageConverter;
    @Nullable
    private final OnErrorConsumer onErrorConsumer;
    private final Map<UUID, CountDownLatch> receiptLatches = new ConcurrentHashMap<>();
    private final Duration receiptTimeout;
    private final ReceiptPolicy receiptPolicy;

    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.UNUSED);
    private final TransferQueue<Frame> connectedFrames = new LinkedTransferQueue<>();
    private final Lock mutex = new ReentrantLock();

    Stomp1dot2Client(URI endpoint, MessageConverter messageConverter, @Nullable OnErrorConsumer onErrorConsumer,
                     Duration receiptTimeout, ReceiptPolicy receiptPolicy
    ) {
        this.endpoint = endpoint;
        this.websocketClient = new JakartaWebsocketClient(endpoint, this::handle);
        this.messageConverter = messageConverter;
        this.onErrorConsumer = onErrorConsumer;
        this.receiptTimeout = receiptTimeout;
        this.receiptPolicy = receiptPolicy;
        this.subscriberSubscriptionFactory = new SubscriberSubscriptionFactory(messageConverter);
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
            if (version == null || !version.equals("1.2")) {
                doClose();
                throw new ConnectionException("Unsupported STOMP version: " + version);
            }
            connectionState.set(ConnectionState.CONNECTED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException(e);
        } finally {
            mutex.unlock();
        }
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
        sendAndAwaitReceiptIfPolicy(sendFrame, ReceiptPolicy.Policy.FOR_SEND);
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
        Frame subscribeFrame = Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", subscription.destination())
                .header("id", subscription.id().toString())
                .header("ack", "auto") // TODO make ack mode configurable
                .build();
        sendAndAwaitReceiptIfPolicy(subscribeFrame, ReceiptPolicy.Policy.FOR_SUBSCRIBE);
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
        Frame unsubscribeFrame = Frame.builder()
                .command(Command.UNSUBSCRIBE)
                .header("id", subscription.id().toString())
                .build();
        sendAndAwaitReceiptIfPolicy(unsubscribeFrame, ReceiptPolicy.Policy.FOR_UNSUBSCRIBE);
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
            sendAndAwaitReceiptIfPolicy(disconnectFrame, ReceiptPolicy.Policy.FOR_DISCONNECT);
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
     * Sends a frame and awaits a receipt if the policy requires it.
     *
     * @param frame  the frame to send
     * @param policy the receipt policy to check
     */
    private void sendAndAwaitReceiptIfPolicy(Frame frame, ReceiptPolicy.Policy policy) {
        if (receiptPolicy.isEnabled(policy)) {
            sendAndAwaitReceipt(frame);
        } else {
            websocketClient.send(frame);
        }
    }

    /**
     * Sends a frame and waits for the corresponding RECEIPT frame.
     *
     * @param frame the frame to send
     * @throws SendException if the receipt is not received within the timeout
     */
    private void sendAndAwaitReceipt(Frame frame) {
        UUID receiptId = UUID.randomUUID();
        frame.headers().addFirst("receipt", receiptId.toString());
        CountDownLatch latch = new CountDownLatch(1);
        receiptLatches.put(receiptId, latch);
        websocketClient.send(frame);
        try {
            if (!latch.await(receiptTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw new ReceiptTimeoutException("Did not receive receipt for id " + receiptId + " within " + receiptTimeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SendException("Interrupted while waiting for receipt", e);
        } finally {
            receiptLatches.remove(receiptId);
        }
    }

    /**
     * Handles an incoming RECEIPT frame.
     * It looks up the corresponding latch and counts it down.
     *
     * @param frame the RECEIPT frame
     */
    private void handleReceipt(Frame frame) {
        UUID receiptId;
        try {
            receiptId = UUID.fromString(frame.headers().get("receipt-id").getFirst());
        } catch (IllegalArgumentException _) {
            log.warn("Received RECEIPT with invalid receipt id: {}", frame);
            return;
        }

        CountDownLatch latch = receiptLatches.get(receiptId);
        if (latch != null) {
            latch.countDown();
        } else {
            log.warn("Received RECEIPT for unknown receipt id: {}", receiptId);
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
            case RECEIPT -> handleReceipt(frame);
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
