package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    static void main() throws URISyntaxException, InterruptedException {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .receiptPolicy(ReceiptPolicy.all())
                .receiptTimeout(Duration.ofSeconds(1))
                .build();

        stompClient.connect();
        CountDownLatch latch = new CountDownLatch(1);
        Model model = new Model(UUID.randomUUID(), "Test");

        stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
            if (m.equals(model))
                latch.countDown();
        });
        stompClient.send("/server/simple/echo", model);

        if (!latch.await(10, TimeUnit.SECONDS))
            System.exit(1);

        stompClient.close();

        Thread.sleep(2000);
    }

}
