package org.schlunzis.zis.stomp.broker.it;

import io.avaje.jex.Jex;
import io.avaje.jex.websocket.WebSocketPlugin;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.jax.JaxConfigurationConsumer;
import org.schlunzis.zis.stomp.broker.connection.jax.JaxSession;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JaxIT {

    private StompBroker<JaxSession> broker;
    private Jex.Server server;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        broker = StompBroker.<JaxSession>builder()
                .hosts("localhost")
                .build();

        WebSocketPlugin wsPlugin = WebSocketPlugin.create()
                .ws("/ws", new JaxConfigurationConsumer(broker));
        server = Jex.create()
                .plugin(wsPlugin)
                .port(36941)
                .start();

        broker.start();

        WebSocketClient webSocketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.start();
    }

    @AfterEach
    void teardown() {
        stompClient.stop();
        broker.close();
        server.shutdown();
    }

    @Test
    void testSubscribe() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                StompHeaders headers = new StompHeaders();
                headers.setHost("localhost");
                stompClient.connectAsync("ws://localhost:36941/ws", (WebSocketHttpHeaders) null, headers, new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        session.subscribe("/topic/test", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return String.class;
                            }

                            @Override
                            public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                                latch.countDown();
                            }
                        });
                        session.send("/topic/test", "Test");
                    }
                }).get(5, TimeUnit.SECONDS);

                assertTrue(latch.await(1, TimeUnit.SECONDS));
                stompClient.stop();
            } catch (Throwable t) {
                fail(t);
            }
        });

        thread.join(5000);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    void testUnsubscribe() throws InterruptedException {
        AtomicBoolean bool = new AtomicBoolean(true);
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                StompHeaders headers = new StompHeaders();
                headers.setHost("localhost");
                stompClient.connectAsync("ws://localhost:36941/ws", (WebSocketHttpHeaders) null, headers, new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
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
                        subscription.unsubscribe();
                        session.send("/topic/test", "Test");
                    }
                }).get(5, TimeUnit.SECONDS);

                assertTrue(bool.get());
                stompClient.stop();
            } catch (Throwable t) {
                fail(t);
            }
        });

        thread.join(5000);
        assertTrue(bool.get());
    }

}
