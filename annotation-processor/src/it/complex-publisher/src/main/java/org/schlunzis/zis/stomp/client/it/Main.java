package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompClient;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.it.sub.PublisherClass;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();

        Publisher publisher = new PublisherClass(stompClient);

        CompletableFuture<Void> future = stompClient.connect();
        future.get(1, TimeUnit.SECONDS);
        CountDownLatch latch = new CountDownLatch(3);
        Model model = new Model(UUID.randomUUID(), "Test");

        Subscription s = stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
                    System.out.println("Received model: " + m);
                    if (m.equals(model))
                        latch.countDown();
                })
                .get(1, TimeUnit.SECONDS);
        publisher.sendSimpleEcho(model);
        Thread.sleep(1000);
        stompClient.unsubscribe(s).get(1, TimeUnit.SECONDS);

        Subscription s2 = stompClient.subscribe("/insight/simple/echo", String.class, m -> {
                    System.out.println("Received string: " + m);
                    if (m.equals(model.toString()))
                        latch.countDown();
                })
                .get(1, TimeUnit.SECONDS);
        publisher.sendAnotherEcho(model.toString());
        Thread.sleep(1000);
        stompClient.unsubscribe(s2).get(1, TimeUnit.SECONDS);

        Subscription s3 = stompClient.subscribe("/insight/simple/echo", String.class, m -> {
                    System.out.println("Received string: " + m);
                    if (m.equals(model.toString()))
                        latch.countDown();
                })
                .get(1, TimeUnit.SECONDS);
        publisher.sendAnotherEchoAsync(model.toString()).join();
        Thread.sleep(1000);
        stompClient.unsubscribe(s3).get(1, TimeUnit.SECONDS);

        if (!latch.await(10, TimeUnit.SECONDS))
            System.exit(1);

        stompClient.close();
    }

}
