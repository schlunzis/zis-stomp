package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompPublisher;
import org.schlunzis.zis.stomp.client.Topic;

@StompPublisher
public interface Publisher {

    @Topic("/server/simple/echo")
    void sendSimpleEcho(Model model);

}
