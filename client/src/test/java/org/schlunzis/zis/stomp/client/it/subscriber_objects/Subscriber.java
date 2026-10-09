package org.schlunzis.zis.stomp.client.it.subscriber_objects;

import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Topic;
import org.schlunzis.zis.stomp.client.it.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

@StompSubscriber(destinationPrefix = "/insight/scheduled/publisher")
public class Subscriber {

    private static final Logger log = LoggerFactory.getLogger(Subscriber.class);

    private final int expectedMessageCount;
    private final CountDownLatch stringLatch;
    private final CountDownLatch modelLatch;

    public Subscriber(int expectedMessageCount) {
        this.expectedMessageCount = expectedMessageCount - 1; // - 1 to have buffer
        log.info("Subscriber with {} expected messages", this.expectedMessageCount);
        this.stringLatch = new CountDownLatch(this.expectedMessageCount);
        this.modelLatch = new CountDownLatch(this.expectedMessageCount);
    }

    public boolean awaitCountsReached() throws InterruptedException {
        return stringLatch.await(expectedMessageCount + 10L, java.util.concurrent.TimeUnit.SECONDS) &&
                modelLatch.await(expectedMessageCount + 10L, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Topic("/string")
    public void receiveString(String message) {
        log.info("Received string message: {}", message);
        stringLatch.countDown();
    }

    @Topic("/model")
    public void receiveModel(Model model) {
        log.info("Received model message: {}", model);
        modelLatch.countDown();
    }

}
