package org.schlunzis.zis.stomp.mock_server.test.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@MessageMapping("/test/client/BasicIT")
@RequiredArgsConstructor
public class BasicITController {

    private final SimpMessagingTemplate template;

    @MessageMapping("/simpleSendAndSubscribe")
    public void simpleSendAndSubscribe(String message) {
        log.info("simpleSendAndSubscribe message: {}", message);
        if ("message".equals(message))
            template.convertAndSend("/insight/client/BasicIT/simpleSendAndSubscribe", "received");
    }

}
