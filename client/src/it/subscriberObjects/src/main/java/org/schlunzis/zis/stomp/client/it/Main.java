package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

/**
 * This verifies that multiple subscribers can coexist and receive their respective messages.
 * It sets up two subscribers with different expected message counts and ensures both receive their messages.
 */
public class Main {

    static void main() throws Exception {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
        CompletableFuture<Void> future = stompClient.connect();
        future.get(1, TimeUnit.SECONDS);

        Subscriber subscriber5 = new Subscriber(5);
        stompClient.subscribe(subscriber5).get(1, TimeUnit.SECONDS);
        Subscriber subscriber1 = new Subscriber(1);
        stompClient.subscribe(subscriber1).get(1, TimeUnit.SECONDS);

        subscriber1.awaitCountsReached();
        stompClient.unsubscribe(subscriber1).get(1, TimeUnit.SECONDS);

        subscriber5.awaitCountsReached();
        stompClient.unsubscribe(subscriber5).get(1, TimeUnit.SECONDS);

        Thread.sleep(2000);

        stompClient.close();
    }

}
