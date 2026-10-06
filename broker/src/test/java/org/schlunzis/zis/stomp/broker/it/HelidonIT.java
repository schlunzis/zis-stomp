package org.schlunzis.zis.stomp.broker.it;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.websocket.WsRouting;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonSession;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonWebsocketListener;
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

public class HelidonIT {

    private StompBroker<HelidonSession> broker;
    private WebServer server;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        broker = StompBroker.<HelidonSession>builder()
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
        stompClient.start();
    }

    @AfterEach
    void teardown() {
        stompClient.stop();
        broker.close();
        server.stop();
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
