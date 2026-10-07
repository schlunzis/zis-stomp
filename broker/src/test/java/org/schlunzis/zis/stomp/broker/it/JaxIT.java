package org.schlunzis.zis.stomp.broker.it;

import io.avaje.jex.Jex;
import io.avaje.jex.websocket.WebSocketPlugin;
import io.avaje.jex.websocket.WsContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.jax.JaxConfigurationConsumer;
import org.schlunzis.zis.stomp.broker.connection.jax.JaxSessionAdapter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JaxIT {

    private StompBroker<WsContext> broker;
    private Jex.Server server;
    private WebSocketStompClient stompClient;
    private StandardWebSocketClient webSocketClient;

    @BeforeEach
    void setup() {
        broker = StompBroker.<WsContext>builder()
                .sessionAdapter(new JaxSessionAdapter())
                .hosts("localhost")
                .build();

        WebSocketPlugin wsPlugin = WebSocketPlugin.create()
                .ws("/ws", new JaxConfigurationConsumer(broker));
        server = Jex.create()
                .plugin(wsPlugin)
                .port(36941)
                .start();

        broker.start();

        webSocketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setTaskScheduler(new SimpleAsyncTaskScheduler());
        stompClient.start();
    }

    @AfterEach
    void teardown() {
        stompClient.stop();
        broker.close();
        server.shutdown();
    }

    @Test
    void testSubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicReference<StompSession> s = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(2);
        StompHeaders headers = new StompHeaders();
        headers.setHost("localhost");
        stompClient.connectAsync("ws://localhost:36941/ws", (WebSocketHttpHeaders) null, headers, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                s.set(session);
                session.setAutoReceipt(true);
                session.subscribe("/topic/test", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        latch.countDown();
                    }
                }).addReceiptTask(() ->
                        session.send("/topic/test", "Test")
                                .addReceiptTask(latch::countDown)
                );
            }
        }).get(1, TimeUnit.SECONDS);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        s.get().disconnect();
    }

    @Test
    void testUnsubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicReference<StompSession> s = new AtomicReference<>(null);
        AtomicBoolean bool = new AtomicBoolean(true);
        CountDownLatch latch = new CountDownLatch(1);
        StompHeaders headers = new StompHeaders();
        headers.setHost("localhost");
        stompClient.connectAsync("ws://localhost:36941/ws", (WebSocketHttpHeaders) null, headers, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                s.set(session);
                session.setAutoReceipt(true);
                StompSession.Subscription subscription = session.subscribe("/topic/test", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        bool.set(false);
                    }
                });
                subscription.addReceiptTask(() ->
                        subscription.unsubscribe().addReceiptTask(() ->
                                session.send("/topic/test", "Test")
                                        .addReceiptTask(latch::countDown)
                        )
                );
            }
        }).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(bool.get());
        s.get().disconnect();
    }

}
