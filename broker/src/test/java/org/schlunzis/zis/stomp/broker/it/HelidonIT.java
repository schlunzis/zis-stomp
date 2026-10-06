package org.schlunzis.zis.stomp.broker.it;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.websocket.WsRouting;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonSession;
import org.schlunzis.zis.stomp.broker.connection.helidon.HelidonWebsocketListener;
import org.schlunzis.zis.stomp.client.StompClient;
import org.schlunzis.zis.stomp.client.Subscription;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class HelidonIT {

    @Test
    void testSubscribe() throws InterruptedException {
        StompBroker<HelidonSession> broker = StompBroker.<HelidonSession>builder()
                .hosts("localhost")
                .build();

        WebServer server = WebServer.builder()
                .port(36941)
                .addRouting(WsRouting.builder()
                        .endpoint("/ws", new HelidonWebsocketListener(broker)))
                .build()
                .start();

        broker.start();

        Thread.sleep(1000);

        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                StompClient client = StompClient.builder()
                        .endpoint(new URI("ws://localhost:36941/ws"))
                        .onError((m, _, _) -> fail(m))
                        .build();
                client.connect().get(5, TimeUnit.SECONDS);

                client.subscribe("/topic/test", String.class, message -> {
                    System.out.println(message);
                    latch.countDown();
                }).get(5, TimeUnit.SECONDS);

                client.send("/topic/test", "Test").get(5, TimeUnit.SECONDS);
                assertTrue(latch.await(1, TimeUnit.SECONDS));
                client.close();
            } catch (Throwable t) {
                fail(t);
            }
        });

        thread.join(5000);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        broker.close();
        server.stop();
    }

    @Test
    void testUnsubscribe() throws InterruptedException {
        StompBroker<HelidonSession> broker = StompBroker.<HelidonSession>builder()
                .hosts("localhost")
                .build();

        WebServer server = WebServer.builder()
                .port(36941)
                .addRouting(WsRouting.builder()
                        .endpoint("/ws", new HelidonWebsocketListener(broker)))
                .build()
                .start();

        broker.start();

        Thread.sleep(1000);

        AtomicBoolean bool = new AtomicBoolean(true);
        Thread thread = Thread.ofVirtual().start(() -> {
            try {
                StompClient client = StompClient.builder()
                        .endpoint(new URI("ws://localhost:36941/ws"))
                        .onError((m, _, _) -> fail(m))
                        .build();
                client.connect().get(5, TimeUnit.SECONDS);

                Subscription subscription = client.subscribe("/topic/test", String.class, _ -> {
                    bool.set(false);
                    fail();
                }).get(5, TimeUnit.SECONDS);
                client.unsubscribe(subscription).get(5, TimeUnit.SECONDS);

                client.send("/topic/test", "Test").get(5, TimeUnit.SECONDS);
                assertTrue(bool.get());
                client.close();
            } catch (Throwable t) {
                fail(t);
            }
        });

        thread.join(5000);
        assertTrue(bool.get());
        broker.close();
        server.stop();
    }

}
