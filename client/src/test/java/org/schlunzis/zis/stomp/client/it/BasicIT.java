package org.schlunzis.zis.stomp.client.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicIT {

    StompClient stompClient;

    @BeforeEach
    void setUp() throws URISyntaxException {
        stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
    }

    @Test
    void simpleSendAndSubscribe() throws InterruptedException {
        CompletableFuture<Void> future = stompClient.connect();
        future.join();
        CountDownLatch latch = new CountDownLatch(1);
        stompClient.subscribe("/insight/client/BasicIT/simpleSendAndSubscribe", String.class, message -> {
            if ("received".equals(message))
                latch.countDown();
        });

        stompClient.send("/server/test/client/BasicIT/simpleSendAndSubscribe", "message");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        stompClient.close();
    }

}
