package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

/**
 * This verifies that multiple subscribers can coexist and receive their respective messages.
 * It sets up two subscribers with different expected message counts and ensures both receive their messages.
 */
public class Main {

    static void main() throws URISyntaxException, InterruptedException {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
        CompletableFuture<Void> future = stompClient.connect();
        future.join();

        Subscriber subscriber5 = new Subscriber(5);
        stompClient.subscribe(subscriber5);
        Subscriber subscriber1 = new Subscriber(1);
        stompClient.subscribe(subscriber1);

        subscriber1.awaitCountsReached();
        stompClient.unsubscribe(subscriber1);

        subscriber5.awaitCountsReached();
        stompClient.unsubscribe(subscriber5);

        Thread.sleep(2000);

        stompClient.close();
    }

}
