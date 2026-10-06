package org.schlunzis.zis.stomp.mock_server.test.client;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("/test/client/BasicIT")
public class BasicITController {

    private final SimpMessagingTemplate template;

    public BasicITController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/simpleSendAndSubscribe")
    public void simpleSendAndSubscribe(String message) {
        if ("message".equals(message))
            template.convertAndSend("/insight/client/BasicIT/simpleSendAndSubscribe", "received");
    }

}
