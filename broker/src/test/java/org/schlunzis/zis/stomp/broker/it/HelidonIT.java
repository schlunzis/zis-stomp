package org.schlunzis.zis.stomp.broker.it;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.websocket.WsRouting;
import io.helidon.websocket.WsSession;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonSessionAdapter;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonWebsocketListener;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
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

public class HelidonIT {

    private StompBroker<WsSession> broker;
    private WebServer server;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        broker = StompBroker.<WsSession>builder()
                .sessionAdapter(new HelidonSessionAdapter())
                .hosts("localhost")
                .build();

        server = WebServer.builder()
                .port(36941)
                .addRouting(WsRouting.builder()
                        .endpoint("/ws", new HelidonWebsocketListener(broker)))
                .build()
                .start();

        broker.start();

        WebSocketClient webSocketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setTaskScheduler(new SimpleAsyncTaskScheduler());
        stompClient.start();
    }

    @AfterEach
    void teardown() {
        stompClient.stop();
        broker.close();
        server.stop();
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
