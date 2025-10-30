package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompClient;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.it.sub.PublisherClass;

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

        PublisherClass publisher = new PublisherClass(stompClient);

        stompClient.connect();
        CountDownLatch latch = new CountDownLatch(2);
        Model model = new Model(UUID.randomUUID(), "Test");

        Subscription s = stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
            System.out.println("Received model: " + m);
            if (m.equals(model))
                latch.countDown();
        });
        publisher.sendSimpleEcho(model);
        Thread.sleep(1000);
        stompClient.unsubscribe(s);

        Subscription s2 = stompClient.subscribe("/insight/simple/echo", String.class, m -> {
            System.out.println("Received string: " + m);
            if (m.equals(model.toString()))
                latch.countDown();
        });
        publisher.sendAnotherEcho(model.toString());
        Thread.sleep(1000);
        stompClient.unsubscribe(s2);

        if (!latch.await(10, TimeUnit.SECONDS))
            System.exit(1);

        stompClient.close();
    }

}
