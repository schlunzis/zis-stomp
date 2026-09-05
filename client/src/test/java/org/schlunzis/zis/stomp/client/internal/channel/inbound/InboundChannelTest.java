package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.OnErrorConsumer;
import org.schlunzis.zis.stomp.client.internal.SubscriptionManager;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InboundChannelTest {

    InboundChannel inboundChannel;

    @BeforeEach
    void setUp() {
        inboundChannel = new InboundChannel();
    }

    @Test
    void testHandleWithoutHandler() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .build();

        assertDoesNotThrow(() -> inboundChannel.handle(frame));
    }

    @Test
    void testHandleWithHandler() {
        OnErrorConsumer consumer = mock(OnErrorConsumer.class);
        AbstractInboundChannelHandler handler = new InboundErrorChannelHandler(consumer);
        inboundChannel.setFirstHandler(handler);

        Frame frame = Frame.builder()
                .command(Command.ERROR)
                .build();

        inboundChannel.handle(frame);

        verify(consumer).accept(any(), any(), isNull());
    }

    @Test
    void testCloseWithoutHandler() {
        assertDoesNotThrow(() -> inboundChannel.close());
    }

    @Test
    void testCloseWithHandler() {
        SubscriptionManager manager = mock(SubscriptionManager.class);
        AbstractInboundChannelHandler handler = new SubscriptionsInboundChannelHandler(manager);
        inboundChannel.setFirstHandler(handler);

        inboundChannel.close();
        verify(manager).clear();
    }

    @Test
    void testWaitingForConnectedFrame() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread.ofVirtual()
                .start(() -> {
                    try {
                        inboundChannel.waitForConnectedFrame();
                        latch.countDown();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        Frame connectedFrame = Frame.builder()
                .command(Command.CONNECTED)
                .build();

        inboundChannel.connected(connectedFrame);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

}
