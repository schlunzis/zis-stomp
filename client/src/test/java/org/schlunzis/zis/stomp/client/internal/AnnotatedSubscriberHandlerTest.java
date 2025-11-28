package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.StringMessageConverter;
import org.schlunzis.zis.stomp.client.Topic;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class AnnotatedSubscriberHandlerTest {

    AnnotatedSubscriberHandler handler;
    SubscriptionManager subscriptionManager;

    @BeforeEach
    void setUp() {
        MessageConverter messageConverter = new StringMessageConverter();
        handler = new AnnotatedSubscriberHandler(messageConverter);
        subscriptionManager = new SubscriptionManager(handler);
        handler.subscriptionManager(subscriptionManager);
    }

    @Test
    void testWithOneSubscriberMethod() {
        class TestSubscriber {
            final CountDownLatch latch = new CountDownLatch(1);

            @Topic("/test")
            public void onTestMessage(String message) {
                assertEquals("Hello, World!", message);
                latch.countDown();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        List<StompSubscription> subscriptions = handler.handle(subscriber);

        assertEquals(1, subscriptions.size());
        StompSubscription subscription = subscriptions.getFirst();
        assertSame(subscriptionManager, subscription.subscriptionManager());
        assertNotNull(subscription.id());
        assertEquals("/test", subscription.destination());
        // test invoker
        SubscriberInvoker invoker = subscription.invoker();
        Frame testFrame = Frame.builder()
                .command(Command.MESSAGE)
                .body("Hello, World!")
                .build();
        invoker.invoke(testFrame);
        assertEquals(0, subscriber.latch.getCount());
    }

    @Test
    void testWithMultipleSubscriberMethodsAndPrefix() {
        @StompSubscriber(destinationPrefix = "/app")
        class TestSubscriber {
            final CountDownLatch latch1 = new CountDownLatch(1);
            final CountDownLatch latch2 = new CountDownLatch(1);

            @Topic("/topic1")
            public void onTopic1(String message) {
                assertEquals("Message 1", message);
                latch1.countDown();
            }

            @Topic("/topic2")
            public void onTopic2(String message) {
                assertEquals("Message 2", message);
                latch2.countDown();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        List<StompSubscription> subscriptions = handler.handle(subscriber);

        assertEquals(2, subscriptions.size());
        for (StompSubscription subscription : subscriptions) {
            SubscriberInvoker invoker = subscription.invoker();
            if (subscription.destination().equals("/app/topic1")) {
                Frame testFrame1 = Frame.builder()
                        .command(Command.MESSAGE)
                        .body("Message 1")
                        .build();
                invoker.invoke(testFrame1);
            } else if (subscription.destination().equals("/app/topic2")) {
                Frame testFrame2 = Frame.builder()
                        .command(Command.MESSAGE)
                        .body("Message 2")
                        .build();
                invoker.invoke(testFrame2);
            } else {
                fail("Unexpected subscription destination: " + subscription.destination());
            }
        }
        assertEquals(0, subscriber.latch1.getCount());
        assertEquals(0, subscriber.latch2.getCount());
    }

    @Test
    void testMethodWithInvalidParameterCount() {
        class InvalidSubscriber {
            @Topic("/invalid")
            public void invalidMethod(String msg, Integer extra) {
                fail("This method should not be invoked");
            }
        }

        InvalidSubscriber subscriber = new InvalidSubscriber();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(subscriber));

        String expectedMessage = "Method invalidMethod must have exactly one parameter";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

}
