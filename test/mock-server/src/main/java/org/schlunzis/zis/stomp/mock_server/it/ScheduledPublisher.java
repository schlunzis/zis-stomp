package org.schlunzis.zis.stomp.mock_server.it;

import org.schlunzis.zis.stomp.mock_server.it.common.Model;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduledPublisher {

    private final SimpMessagingTemplate template;

    public ScheduledPublisher(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Scheduled(fixedRate = 1000)
    public void send() {
        this.template.convertAndSend("/insight/scheduled/publisher/string", "scheduled message");
        this.template.convertAndSend("/insight/scheduled/publisher/model", new Model(UUID.randomUUID(), "scheduled model message"));
    }

}
