package org.schlunzis.zis.stomp.client.it;

import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Topic;

import java.util.concurrent.CountDownLatch;

@StompSubscriber(destinationPrefix = "/insight/scheduled/publisher/")
public class Subscriber {

    private final int expectedMessageCount;
    private final CountDownLatch stringLatch;
    private final CountDownLatch modelLatch;

    public Subscriber(int expectedMessageCount) {
        this.expectedMessageCount = expectedMessageCount;
        this.stringLatch = new CountDownLatch(expectedMessageCount);
        this.modelLatch = new CountDownLatch(expectedMessageCount);
    }

    public boolean awaitCountsReached() throws InterruptedException {
        return stringLatch.await(expectedMessageCount + 10L, java.util.concurrent.TimeUnit.SECONDS) &&
                modelLatch.await(expectedMessageCount + 10L, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Topic("string")
    public void receiveString(String message) {
        System.out.println("Received string message: " + message);
        if (stringLatch.getCount() == 0) {
            System.out.println("Received more string messages than expected!");
            System.exit(2); // Fail the test if more messages are received than expected
        }
        stringLatch.countDown();
    }

    @Topic("model")
    public void receiveModel(Model model) {
        System.out.println("Received model message: " + model);
        if (modelLatch.getCount() == 0) {
            System.out.println("Received more model messages than expected!");
            System.exit(2); // Fail the test if more messages are received than expected
        }
        modelLatch.countDown();
    }

}
