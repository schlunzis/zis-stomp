package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.ConnectionException;
import org.schlunzis.zis.stomp.client.StringMessageConverter;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.Topic;
import org.schlunzis.zis.stomp.client.internal.channel.inbound.InboundChannel;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundChannel;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Stomp1dot2ClientTest {

    @Mock
    WebSocketClient webSocketClient;
    @Mock
    InboundChannel inboundChannel;
    @Mock
    OutboundChannel outboundChannel;
    @Mock
    SubscriptionManager subscriptionManager;

    @Test
    void testBeforeConnection() throws URISyntaxException {
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );
        Object dummy = new Object();
        @SuppressWarnings("DataFlowIssue")
        Subscription subscription = new StompSubscription(
                null,
                UUID.randomUUID(),
                "/topic",
                null
        );

        assertThrows(IllegalStateException.class, () -> client.send("/topic", "Message"));
        assertThrows(IllegalStateException.class, () -> client.send("/topic", dummy));
        assertThrows(IllegalStateException.class, () -> client.subscribe("/topic", String.class, _ -> fail()));
        assertThrows(IllegalStateException.class, () -> client.unsubscribe(subscription));
        assertDoesNotThrow(client::close);
    }

    @Test
    void testConnectUnreachableURI() throws URISyntaxException {
        doThrow(new ConnectionException("Message")).when(webSocketClient).connect(anyMap());

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://unreachable:9999/ws"),
                new StringMessageConverter(),
                null,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        CompletableFuture<Void> future = client.connect();
        CompletionException e = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(ConnectionException.class, e.getCause());
    }

    @Test
    void testConnectInvalidProtocol() throws URISyntaxException {
        doThrow(new ConnectionException("Message")).when(webSocketClient).connect(anyMap());

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("http://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        CompletableFuture<Void> future = client.connect();
        CompletionException e = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(ConnectionException.class, e.getCause());
    }

    @Test
    void testDoubleConnect() throws URISyntaxException, InterruptedException {
        when(inboundChannel.waitForConnectedFrame()).thenReturn(Frame.builder()
                .command(Command.CONNECTED)
                .header("version", "1.2")
                .build());

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );
        CompletableFuture<Void> future = client.connect();
        future.join();

        future = client.connect();
        CompletionException e = assertThrows(CompletionException.class, future::join);
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }

    @Test
    void testCloseBeforeConnect() throws URISyntaxException {
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        assertDoesNotThrow(client::close);
    }

    @Test
    void testCloseAfterConnect() throws URISyntaxException, ConnectionException, InterruptedException {
        when(inboundChannel.waitForConnectedFrame()).thenReturn(Frame.builder()
                .command(Command.CONNECTED)
                .header("version", "1.2")
                .build());

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );
        CompletableFuture<Void> future = client.connect();
        future.join();

        assertDoesNotThrow(client::close);
    }

    @Test
    void testCloseDouble() throws URISyntaxException, ConnectionException, InterruptedException {
        when(inboundChannel.waitForConnectedFrame()).thenReturn(Frame.builder()
                .command(Command.CONNECTED)
                .header("version", "1.2")
                .build());

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );
        CompletableFuture<Void> future = client.connect();
        future.join();
        client.close();

        assertDoesNotThrow(client::close);
    }

    @Test
    void testSendWithoutConnect() throws URISyntaxException {
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        assertThrows(IllegalStateException.class, () -> client.send("/topic", "Message"));
    }

    @Test
    void testSubscribeWithoutConnect() throws URISyntaxException {
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        assertThrows(IllegalStateException.class, () -> client.subscribe("/topic", String.class,
                message -> fail()));
    }

    @Test
    void testMessageConverter() throws URISyntaxException {
        StringMessageConverter messageConverter = new StringMessageConverter();
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                messageConverter,
                null,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );

        assertSame(messageConverter, client.messageConverter());
    }

    @Test
    void testChangingHashCodeOfSubscriberObject() throws URISyntaxException, InterruptedException {
        when(inboundChannel.waitForConnectedFrame()).thenReturn(Frame.builder()
                .command(Command.CONNECTED)
                .header("version", "1.2")
                .build());
        when(subscriptionManager.hasSubscriptionsForSubscriber(any())).thenReturn(Boolean.FALSE);

        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                subscriptionManager,
                webSocketClient,
                inboundChannel,
                outboundChannel,
                ForkJoinPool.commonPool()
        );
        CompletableFuture<Void> future = client.connect();
        future.join();

        class MutableHashCodeSubscriber {
            int hashCode = 1;

            @Override
            public int hashCode() {
                return hashCode;
            }

            @Topic("/insight/scheduled/publisher/string")
            public void handleStringMessage(String message) {
            }
        }

        MutableHashCodeSubscriber subscriber = new MutableHashCodeSubscriber();
        client.subscribe(subscriber);

        subscriber.hashCode = 2;

        assertDoesNotThrow(() -> client.unsubscribe(subscriber));
    }

}
