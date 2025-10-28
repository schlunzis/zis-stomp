package org.schlunzis.zis.stomp.mock_server.it;

import lombok.RequiredArgsConstructor;
import org.schlunzis.zis.stomp.mock_server.it.common.Model;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduledPublisher {

    private final SimpMessagingTemplate template;

    @Scheduled(fixedRate = 1000)
    public void send() {
        this.template.convertAndSend("/insight/scheduled/publisher/string", "scheduled message");
        this.template.convertAndSend("/insight/scheduled/publisher/model", new Model(UUID.randomUUID(), "scheduled model message"));
    }

}
