package org.schlunzis.zis.stomp.mock_server;

import lombok.RequiredArgsConstructor;
import org.schlunzis.zis.stomp.mock_server.it.common.Model;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@MessageMapping("/simple")
@RequiredArgsConstructor
public class SimpleController {

    private final SimpMessagingTemplate template;

    @MessageMapping("/echo")
    public void echo(Model message) {
        if (message == null ||
                message.message() == null || message.message().isEmpty() ||
                message.id() == null || message.id().equals(new UUID(0, 0))) {
            throw new IllegalArgumentException();
        }

        this.template.convertAndSend("/insight/simple/echo", message);
    }

}
