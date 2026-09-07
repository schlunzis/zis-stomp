package org.schlunzis.zis.stomp.client.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.StompClient;
import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.Topic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.*;

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
    void simpleSendAndSubscribe() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> future = stompClient.connect();
        future.join();
        CountDownLatch latch = new CountDownLatch(1);
        stompClient.subscribe("/insight/client/BasicIT/simpleSendAndSubscribe", String.class, message -> {
                    if ("received".equals(message))
                        latch.countDown();
                })
                .get(1, TimeUnit.SECONDS);

        stompClient.send("/server/test/client/BasicIT/simpleSendAndSubscribe", "message")
                .get(1, TimeUnit.SECONDS);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        stompClient.close();
    }

    @Test
    void testReadme1() throws URISyntaxException, ExecutionException, InterruptedException {
        StompClient client = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
        client.connect().join();

        client.send("/app/hello", "Hello, World!").join();
        Subscription sub = client.subscribe("/topic/greetings", String.class,
                message -> System.out.println("Received: " + message)).get();
        // Do other stuff and listen for messages...
        client.unsubscribe(sub).join();

        client.close();
    }

    @Test
    void testReadme2() throws URISyntaxException {
        StompClient client = StompClient.builder()
                .endpoint(new URI("ws://localhost:8080/ws"))
                .build();
        client.connect().join();

        Controller controller = new Controller();
        client.subscribe(controller).join();
        // Do other stuff and listen for messages...
        client.unsubscribe(controller).join();
    }

    @StompSubscriber(destinationPrefix = "/topic")
    public static class Controller {
        @Topic("/greetings")
        public void onMessage(String message) {
            System.out.println("Received message in Controller: " + message);
        }

        @Topic("/model")
        public void onModelMessage(Model model) {
            System.out.println("Received model in Controller: " + model);
        }
    }

    public record Model() {
    }

}
