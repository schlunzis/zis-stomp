package org.schlunzis.zis.stomp.broker.it;

import io.avaje.jex.Jex;
import io.avaje.jex.websocket.WebSocketPlugin;
import io.avaje.jex.websocket.WsContext;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;
import org.schlunzis.zis.stomp.client.StompClient;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JaxIT {

    @Test
    void test() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
        StompBroker<JaxSession> broker = StompBroker.<JaxSession>builder()
                .build();

        WebSocketPlugin wsPlugin = WebSocketPlugin.create()
                .ws("/ws", ws -> ws
                        .onOpen(ctx -> broker.onOpen(new JaxSession(ctx)))
                        .onMessage(ctx -> broker.onMessage(new JaxSession(ctx), new StringReader(ctx.message())))
                        .onClose(ctx -> broker.onClose(new JaxSession(ctx)))
                        .onError(ctx -> broker.onError(new JaxSession(ctx), ctx.error()))
                );
        Jex.Server server = Jex.create()
                .plugin(wsPlugin)
                .port(36941)
                .start();

        broker.start();


        StompClient client = StompClient.builder()
                .endpoint(new URI("ws://localhost:36941/ws"))
                .build();
        client.connect().get(5, TimeUnit.SECONDS);

        CountDownLatch latch = new CountDownLatch(1);
        client.subscribe("/topic/test", String.class, message -> {
            System.out.println(message);
            latch.countDown();
        }).get(5, TimeUnit.SECONDS);

        client.send("/topic/test", "Test").get(5, TimeUnit.SECONDS);
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        client.close();
        broker.close();
        server.shutdown();
    }

    private record JaxSession(WsContext context) implements WebsocketSession {
        @Override
        public void send(@NonNull String message) {
            context.send(message);
        }

        @Override
        public void close() {
            context.closeSession();
        }
    }

}
