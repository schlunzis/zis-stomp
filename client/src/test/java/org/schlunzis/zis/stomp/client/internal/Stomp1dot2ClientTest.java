package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class Stomp1dot2ClientTest {

    @Test
    void testBeforeConnection() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
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
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://unreachable:9999/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertThrows(ConnectionException.class, client::connect);
    }

    @Test
    void testConnectInvalidProtocol() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("http://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertThrows(ConnectionException.class, client::connect);
    }

    @Test
    void testDoubleConnect() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );
        client.connect();

        assertThrows(IllegalStateException.class, client::connect);
    }

    @Test
    void testCloseBeforeConnect() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertDoesNotThrow(client::close);
    }

    @Test
    void testCloseAfterConnect() throws URISyntaxException, ConnectionException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );
        client.connect();

        assertDoesNotThrow(client::close);
    }

    @Test
    void testCloseDouble() throws URISyntaxException, ConnectionException {
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );
        client.connect();
        client.close();

        assertDoesNotThrow(client::close);
    }

    @Test
    void testSendWithoutConnect() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertThrows(IllegalStateException.class, () -> client.send("/topic", "Message"));
    }

    @Test
    void testSubscribeWithoutConnect() throws URISyntaxException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertThrows(IllegalStateException.class, () -> client.subscribe("/topic", String.class,
                message -> fail()));
    }

    @Test
    void testGetMessageConverter() throws URISyntaxException {
        StringMessageConverter messageConverter = new StringMessageConverter();
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                messageConverter,
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );

        assertSame(messageConverter, client.getMessageConverter());
    }

    @Test
    void testChangingHashCodeOfSubscriberObject() throws URISyntaxException, ConnectionException, InterruptedException {
        @SuppressWarnings("resource")
        Stomp1dot2Client client = new Stomp1dot2Client(
                new URI("ws://localhost:8080/ws"),
                new StringMessageConverter(),
                null,
                Duration.ofSeconds(10),
                ReceiptPolicy.none()
        );
        client.connect();

        CountDownLatch latch = new CountDownLatch(2);
        class MutableHashCodeSubscriber {
            int hashCode = 1;

            @Override
            public int hashCode() {
                return hashCode;
            }

            @Topic("/insight/scheduled/publisher/string")
            public void handleStringMessage(String message) {
                if (latch.getCount() == 0) {
                    fail("Received more messages than expected");
                } else {
                    latch.countDown();
                }
            }
        }

        MutableHashCodeSubscriber subscriber = new MutableHashCodeSubscriber();
        client.subscribe(subscriber);

        subscriber.hashCode = 2;
        if (!latch.await(4, TimeUnit.SECONDS)) {
            fail("Did not receive messages in time");
        }

        assertDoesNotThrow(() -> client.unsubscribe(subscriber));
        Thread.sleep(2000);
    }

}
