package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberInvokerTest {

    @Mock
    MessageConverter messageConverter;

    @Test
    void testInvokeWithValidPayload() throws NoSuchMethodException, InterruptedException {
        when(messageConverter.convertToType(
                "{\"field\":\"test\"}",
                TestPayload.class
        )).thenReturn(new TestPayload("test"));

        CountDownLatch latch = new CountDownLatch(1);
        TestSubscriber subscriber = new TestSubscriber(latch);
        SubscriberInvoker invoker = new SubscriberInvoker(
                messageConverter,
                TestPayload.class,
                TestSubscriber.class.getMethod("onMessage", TestPayload.class),
                subscriber
        );

        String jsonPayload = "{\"field\":\"test\"}";
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .body(jsonPayload)
                .build();

        invoker.invoke(frame);

        if (!latch.await(1, TimeUnit.SECONDS)) {
            fail();
        }
    }

    @Test
    void testInvokeWithStringPayload() throws NoSuchMethodException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TestSubscriber subscriber = new TestSubscriber(latch);
        SubscriberInvoker invoker = new SubscriberInvoker(
                messageConverter,
                String.class,
                TestSubscriber.class.getMethod("onMessage", String.class),
                subscriber
        );

        String stringPayload = "test";
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .body(stringPayload)
                .build();

        invoker.invoke(frame);

        if (!latch.await(1, TimeUnit.SECONDS)) {
            fail();
        }

        verify(messageConverter, never()).convertToType(any(), any());
    }

    @Test
    void testInvokeWithConsumer() throws InterruptedException {
        when(messageConverter.convertToType(
                "{\"field\":\"test\"}",
                TestPayload.class
        )).thenReturn(new TestPayload("test"));

        CountDownLatch latch = new CountDownLatch(1);
        TestSubscriber subscriber = new TestSubscriber(latch);
        SubscriberInvoker<TestPayload> invoker = new SubscriberInvoker<>(
                messageConverter,
                TestPayload.class,
                subscriber::onMessage
        );

        String jsonPayload = "{\"field\":\"test\"}";
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .body(jsonPayload)
                .build();

        invoker.invoke(frame);

        if (!latch.await(1, TimeUnit.SECONDS)) {
            fail();
        }
    }

    record TestPayload(
            String field
    ) {
    }

    record TestSubscriber(CountDownLatch latch) {
        public void onMessage(TestPayload payload) {
            assertNotNull(payload);
            assertNotNull(payload.field);
            assertEquals("test", payload.field);
            latch.countDown();
        }

        public void onMessage(String payload) {
            assertNotNull(payload);
            assertEquals("test", payload);
            latch.countDown();
        }
    }

}
