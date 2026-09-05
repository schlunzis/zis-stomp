package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StringMessageConverter;
import org.schlunzis.zis.stomp.client.Topic;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionManagerTest {

    MessageConverter messageConverter = new StringMessageConverter();
    AnnotatedSubscriberHandler annotatedSubscriberHandler = new AnnotatedSubscriberHandler(messageConverter);

    SubscriptionManager subscriptionManager;

    @BeforeEach
    void setUp() {
        subscriptionManager = new SubscriptionManager(annotatedSubscriberHandler);
        annotatedSubscriberHandler.subscriptionManager(subscriptionManager);
    }

    @Test
    void testCreateAnnotatedSubscriptions() {
        class TestSubscriber {
            @Topic("/topic/test")
            public void handleTestMessage(String ignored) {
                fail();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        Set<StompSubscription> subscriptions = subscriptionManager.createAnnotatedSubscriptions(subscriber);

        assertEquals(1, subscriptions.size());
        StompSubscription subscription = subscriptions.iterator().next();
        assertNotNull(subscription.id());
        assertEquals("/topic/test", subscription.destination());
        assertSame(subscriptionManager, subscription.subscriptionManager());

        assertTrue(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));
    }

    @Test
    void testCreateProgrammaticSubscription() {
        SubscriberInvoker<String> invoker = new SubscriberInvoker<>(
                messageConverter,
                String.class,
                _ -> fail()
        );

        StompSubscription subscription = subscriptionManager.create("/topic/programmatic", invoker);

        assertNotNull(subscription.id());
        assertEquals("/topic/programmatic", subscription.destination());
        assertSame(subscriptionManager, subscription.subscriptionManager());

        assertTrue(subscriptionManager.contains(subscription.id()));
    }

    @Test
    void testRemoveProgrammaticSubscription() {
        SubscriberInvoker<String> invoker = new SubscriberInvoker<>(
                messageConverter,
                String.class,
                _ -> fail()
        );

        StompSubscription subscription = subscriptionManager.create("/topic/to-remove", invoker);
        assertTrue(subscriptionManager.contains(subscription.id()));

        subscriptionManager.remove(subscription);
        assertFalse(subscriptionManager.contains(subscription.id()));
    }

    @Test
    void testRemoveAnnotatedSubscriptions() {
        class TestSubscriber {
            @Topic("/topic/test1")
            public void handleTestMessage1(String ignored) {
                fail();
            }

            @Topic("/topic/test2")
            public void handleTestMessage2(String ignored) {
                fail();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        Set<StompSubscription> subscriptions = subscriptionManager.createAnnotatedSubscriptions(subscriber);
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));

        Optional<Set<StompSubscription>> opt = subscriptionManager.remove(subscriber);
        assertTrue(opt.isPresent());
        assertEquals(subscriptions, opt.get());
        assertFalse(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));
    }

    @Test
    void testRemoveAnnotatedSubscriptionsNoop() {
        class TestSubscriber {
            @Topic("/topic/test")
            public void handleTestMessage(String ignored) {
                fail();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        assertFalse(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));

        Optional<Set<StompSubscription>> opt = subscriptionManager.remove(subscriber);
        assertTrue(opt.isEmpty());
    }

    @Test
    void testHandleMessageValid() {
        CountDownLatch latch = new CountDownLatch(1);
        class TestSubscriber {
            @Topic("/topic/test")
            public void handleTestMessage(String message) {
                assertEquals("Hello, World!", message);
                latch.countDown();
            }
        }

        TestSubscriber subscriber = new TestSubscriber();
        Set<StompSubscription> subscriptions = subscriptionManager.createAnnotatedSubscriptions(subscriber);

        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .header("subscription", subscriptions.iterator().next().id().toString())
                .body("Hello, World!")
                .build();
        subscriptionManager.handleMessage(frame);

        assertEquals(0, latch.getCount());
    }

    @Test
    void testHandleMessageInvalidSubscriptionId() {
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .header("subscription", "invalid-uuid")
                .body("Hello, World!")
                .build();

        assertDoesNotThrow(() -> subscriptionManager.handleMessage(frame));
    }

    @Test
    void testHandleMessageMissingSubscriptionId() {
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .body("Hello, World!")
                .build();

        assertDoesNotThrow(() -> subscriptionManager.handleMessage(frame));
    }

    @Test
    void testHandleMessageNoSubscriptionFound() {
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .header("subscription", UUID.randomUUID().toString())
                .body("Hello, World!")
                .build();

        assertDoesNotThrow(() -> subscriptionManager.handleMessage(frame));
    }

    @Test
    void testClear() {
        SubscriberInvoker<String> invoker = new SubscriberInvoker<>(
                messageConverter,
                String.class,
                _ -> fail()
        );
        class TestSubscriber {
            @Topic("/topic/test")
            public void handleTestMessage(String ignored) {
                fail();
            }
        }

        StompSubscription subscription = subscriptionManager.create("/topic/to-clear", invoker);
        assertTrue(subscriptionManager.contains(subscription.id()));
        TestSubscriber subscriber = new TestSubscriber();
        subscriptionManager.createAnnotatedSubscriptions(subscriber);
        assertTrue(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));

        subscriptionManager.clear();
        assertFalse(subscriptionManager.contains(subscription.id()));
        assertFalse(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));
    }

    @Test
    void testChangingHashcodeSubscriber() {
        class TestSubscriber {
            private String id;

            public TestSubscriber(String id) {
                this.id = id;
            }

            @Topic("/topic/test")
            public void handleTestMessage(String ignored) {
                fail();
            }

            @Override
            public int hashCode() {
                return id.hashCode();
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;

                TestSubscriber that = (TestSubscriber) o;
                return Objects.equals(id, that.id);
            }
        }
        TestSubscriber subscriber = new TestSubscriber("first");
        subscriptionManager.createAnnotatedSubscriptions(subscriber);
        assertTrue(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));

        subscriber.id = "second";

        assertTrue(subscriptionManager.hasSubscriptionsForSubscriber(subscriber));
    }

}
