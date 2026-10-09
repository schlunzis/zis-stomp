package org.schlunzis.zis.stomp.client.it.receipts;

import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.Jackson3MessageConverter;
import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.StompClient;
import org.schlunzis.zis.stomp.client.it.model.Model;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class ReceiptsIT {

    @Test
    void test() throws Exception {
        StompClient stompClient = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .receiptPolicy(ReceiptPolicy.all())
                .receiptTimeout(Duration.ofSeconds(1))
                .messageConverter(new Jackson3MessageConverter())
                .build();

        CompletableFuture<Void> future = stompClient.connect();
        future.get(1, TimeUnit.SECONDS);
        CountDownLatch latch = new CountDownLatch(1);
        Model model = new Model(UUID.randomUUID(), "Test");

        stompClient.subscribe("/insight/simple/echo", Model.class, m -> {
                    if (m.equals(model))
                        latch.countDown();
                })
                .get(1, TimeUnit.SECONDS);
        stompClient.send("/server/simple/echo", model)
                .get(1, TimeUnit.SECONDS);

        if (!latch.await(10, TimeUnit.SECONDS))
            fail("Message not received");

        stompClient.close();
    }

}
