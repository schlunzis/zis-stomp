package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.AvajeJsonbMessageConverter;
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
        if (!(messageConverter instanceof AvajeJsonbMessageConverter))
            throw new IllegalStateException("messageConverter is not of type AvajeJsonbMessageConverter");
    }

}
