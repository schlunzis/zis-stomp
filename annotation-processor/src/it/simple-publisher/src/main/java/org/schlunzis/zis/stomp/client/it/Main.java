package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws URISyntaxException, InterruptedException, IllegalAccessException {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();

        PublisherImpl publisher = new PublisherImpl(stompClient);

        stompClient.connect();
        CountDownLatch latch = new CountDownLatch(1);
        Model model = new Model(UUID.randomUUID(), "Test");

        stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
            if (m.equals(model))
                latch.countDown();
        });
        publisher.sendSimpleEcho(model);

        if (!latch.await(10, TimeUnit.SECONDS))
            System.exit(1);

        stompClient.close();
    }

}
