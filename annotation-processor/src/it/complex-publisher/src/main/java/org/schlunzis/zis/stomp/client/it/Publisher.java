package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompPublisher;
import org.schlunzis.zis.stomp.client.Topic;

import java.util.concurrent.CompletableFuture;

@StompPublisher(
        destinationPrefix = "/server",
        packageName = "org.schlunzis.zis.stomp.client.it.sub",
        typeName = "PublisherClass"
)
public interface Publisher {

    @Topic("/simple/echo")
    void sendSimpleEcho(Model model);

    @Topic("/simple/echo/string")
    void sendAnotherEcho(String model);

    @Topic("/simple/echo/string")
    CompletableFuture<Void> sendAnotherEchoAsync(String model);

}
