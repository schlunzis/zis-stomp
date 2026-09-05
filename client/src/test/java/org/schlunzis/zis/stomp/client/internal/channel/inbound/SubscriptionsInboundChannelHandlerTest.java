package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.internal.SubscriptionManager;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionsInboundChannelHandlerTest {

    @Mock
    SubscriptionManager subscriptionManager;

    @Test
    void testHandle() {
        SubscriptionsInboundChannelHandler handler = new SubscriptionsInboundChannelHandler(subscriptionManager);
        Frame receiptFrame = Frame.builder()
                .command(Command.MESSAGE)
                .build();

        handler.handle(receiptFrame);

        verify(subscriptionManager).handleMessage(receiptFrame);
    }

    @Test
    void testHandleNonReceiptFrame() {
        SubscriptionsInboundChannelHandler handler = new SubscriptionsInboundChannelHandler(subscriptionManager);
        Frame messageFrame = Frame.builder()
                .command(Command.CONNECTED)
                .build();

        handler.handle(messageFrame);

        verify(subscriptionManager, never()).handleMessage(messageFrame);
    }

    @Test
    void testClose() {
        SubscriptionsInboundChannelHandler handler = new SubscriptionsInboundChannelHandler(subscriptionManager);
        handler.close();
        verify(subscriptionManager).clear();
    }

}
