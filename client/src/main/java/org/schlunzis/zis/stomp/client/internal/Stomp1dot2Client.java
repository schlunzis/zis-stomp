package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.*;
import org.schlunzis.zis.stomp.client.internal.channel.inbound.InboundChannel;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundChannel;
import org.schlunzis.zis.stomp.client.internal.interaction.EmptyInteractionContext;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.protocol.Frames;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class Stomp1dot2Client implements StompClient {

    private static final Logger log = LoggerFactory.getLogger(Stomp1dot2Client.class);
    private static final String STRING_CONTENT_TYPE = "text/plain;charset=UTF-8";

    private final URI endpoint;
    private final MessageConverter messageConverter;
    private final SubscriptionManager subscriptionManager;
    private final WebSocketClient websocketClient;
    private final InboundChannel inboundChannel;
    private final OutboundChannel outboundChannel;

    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.UNUSED);
    private final Lock mutex = new ReentrantLock();

    public Stomp1dot2Client(URI endpoint, MessageConverter messageConverter, SubscriptionManager subscriptionManager, WebSocketClient webSocketClient,
                            InboundChannel inboundChannel, OutboundChannel outboundChannel
    ) {
        this.endpoint = endpoint;
        this.messageConverter = messageConverter;
        this.subscriptionManager = subscriptionManager;
        this.websocketClient = webSocketClient;
        this.inboundChannel = inboundChannel;
        this.outboundChannel = outboundChannel;
    }

    // ########
    // CONNECT
    // ########

    @Override
    public CompletableFuture<Void> connect() throws ConnectionException {
        FrameBuilder connectFrame = Frames.connect(endpoint);
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
                FrameBuilder connectFrame = Frames.connect(endpoint, login, passcode);
                yield CompletableFuture.runAsync(() -> doConnect(connectFrame, Map.of()));
            }
            case HTTP_BASIC -> {
                String credentials = login + ":" + passcode;
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                String authHeaderValue = "Basic " + encodedCredentials;
                FrameBuilder connectFrame = Frames.connect(endpoint);
                yield CompletableFuture.runAsync(() -> doConnect(connectFrame, Map.of("Authorization", List.of(authHeaderValue))));
            }
        };
    }

    private void doConnect(FrameBuilder connectFrame, Map<String, List<String>> connectHeaders) throws ConnectionException {
        mutex.lock();
        try {
            if (connectionState.get() != ConnectionState.UNUSED) {
                throw new IllegalStateException("Client has already connected before. Current state: " + connectionState.get());
            }
            connectionState.set(ConnectionState.CONNECTING);

            websocketClient.connect(connectHeaders);
            outboundChannel.handle(connectFrame, new EmptyInteractionContext<>());

            Frame connectedFrame = inboundChannel.waitForConnectedFrame();
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
        Objects.requireNonNull(destination, "destination must not be null");
        Objects.requireNonNull(body, "body must not be null");
        SendContext sendContext = SendContext.create(destination, body)
                .header("content-type", STRING_CONTENT_TYPE);
        send(sendContext);
    }

    @Override
    public void send(String destination, Object body) {
        Objects.requireNonNull(destination, "destination must not be null");
        Objects.requireNonNull(body, "body must not be null");
        SendContext sendContext = SendContext.create(destination, body);
        send(sendContext);
    }

    @Override
    public void send(SendContext context) {
        Objects.requireNonNull(context, "SendContext must not be null");
        ensureConnected();
        FrameBuilder builder = Frame.builder()
                .command(Command.SEND)
                .header("destination", context.destination())
                .body(convertBodyToString(context.body()))
                .header("content-type", context.body() instanceof String
                        ? STRING_CONTENT_TYPE
                        : messageConverter.contentType()
                );
        outboundChannel.handle(builder, context);
    }

    // ##########
    // SUBSCRIBE
    // ##########

    @Override
    public <T> Subscription subscribe(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        Objects.requireNonNull(destination, "destination must not be null");
        Objects.requireNonNull(payloadType, "payloadType must not be null");
        Objects.requireNonNull(messageHandler, "messageHandler must not be null");
        SubscribeContext<T> subscribeContext = SubscribeContext.create(
                destination,
                payloadType,
                messageHandler
        );
        return subscribe(subscribeContext);
    }

    @Override
    public <T> Subscription subscribe(SubscribeContext<T> context) {
        Objects.requireNonNull(context, "SubscribeContext must not be null");
        ensureConnected();

        Subscription subscription = subscriptionManager.create(context.destination(),
                new SubscriberInvoker(messageConverter, context.payloadType(), context.messageHandler()));

        FrameBuilder builder = Frames.subscribe(
                context.destination(),
                subscription.id().toString(),
                "auto"
        );
        outboundChannel.handle(builder, context);
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
            subscriptions.forEach(s -> {
                FrameBuilder subscribeFrame = Frames.subscribe(s.destination(), s.id().toString(), "auto");
                outboundChannel.handle(subscribeFrame, new EmptyInteractionContext<>());
            });
        } finally {
            mutex.unlock();
        }
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
        FrameBuilder unsubscribeFrame = Frames.unsubscribe(subscription.id().toString());
        outboundChannel.handle(unsubscribeFrame, new EmptyInteractionContext<>());
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

            FrameBuilder disconnectFrame = Frames.disconnect();
            outboundChannel.handle(disconnectFrame, new EmptyInteractionContext<>());
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
            outboundChannel.close();
            inboundChannel.close();
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

    private String convertBodyToString(Object body) {
        if (body instanceof String s) {
            return s;
        }
        return messageConverter.convertToString(body);
    }

    /// If the client is not connected, this method throws an [IllegalStateException].
    /// This method is used to ensure that operations that require a connected client
    /// are only performed when the client is indeed connected.
    ///
    /// @throws IllegalStateException if the client is not connected
    private void ensureConnected() {
        if (connectionState.get() != ConnectionState.CONNECTED) {
            log.trace("ensureConnected() - client is not connected");
            throw new IllegalStateException("Client is not connected");
        }
        log.trace("ensureConnected() - client is connected");
    }

}
