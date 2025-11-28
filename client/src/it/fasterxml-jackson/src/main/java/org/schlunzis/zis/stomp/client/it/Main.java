package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.Jackson2MessageConverter;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StompClient;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws Exception {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();

        MessageConverter messageConverter = stompClient.messageConverter();
        if (!(messageConverter instanceof Jackson2MessageConverter))
            throw new IllegalStateException("messageConverter is not of type Jackson2MessageConverter");

        CompletableFuture<Void> future = stompClient.connect();
        future.get(1, TimeUnit.SECONDS);
        CountDownLatch latch = new CountDownLatch(1);
        Model model = new Model(UUID.randomUUID(), "Test");

        stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
                    if (m.equals(model))
                        latch.countDown();
                }).
                get(1, TimeUnit.SECONDS);
        stompClient.send("/server/simple/echo", model)
                .get(1, TimeUnit.SECONDS);

        if (!latch.await(10, TimeUnit.SECONDS))
            System.exit(1);

        stompClient.close();
    }

}
