package org.schlunzis.zis.stomp.client.it.subscriber_objects;

import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubscriberObjectsIT {

    @Test
    void test() throws Exception {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
        CompletableFuture<Void> future = stompClient.connect();
        future.get(1, TimeUnit.SECONDS);

        Subscriber subscriber = new Subscriber(5);
        stompClient.subscribe(subscriber).get(1, TimeUnit.SECONDS);

        assertTrue(subscriber.awaitCountsReached());
        stompClient.unsubscribe(subscriber).get(1, TimeUnit.SECONDS);

        Thread.sleep(2000);

        stompClient.close();
    }

}
