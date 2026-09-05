package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.internal.Receiptable;
import org.schlunzis.zis.stomp.client.internal.interaction.EmptyInteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;
import org.schlunzis.zis.stomp.common.protocol.FrameBuilder;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptOutboundChannelHandlerTest {

    ReceiptOutboundChannelHandler handler;

    @Mock
    ReceiptManager receiptManager;

    @BeforeEach
    void setUp() {
        handler = new ReceiptOutboundChannelHandler(receiptManager);
    }

    @Test
    void testHandleWithoutReceipt() {
        when(receiptManager.attachReceiptIfPolicyEnabled(any())).thenReturn(Optional.empty());

        InteractionContext<?> context = new EmptyInteractionContext<>();
        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        handler.handle(frameBuilder, context);

        Receiptable receiptable = context.receiptable();
        assertNull(receiptable);
    }

    @Test
    void testHandleWithReceipt() {
        Receiptable receiptable = new Receiptable(Duration.ofSeconds(5), new CountDownLatch(1));
        when(receiptManager.attachReceiptIfPolicyEnabled(any())).thenReturn(Optional.of(receiptable));

        InteractionContext<?> context = new EmptyInteractionContext<>();
        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        handler.handle(frameBuilder, context);

        Receiptable r = context.receiptable();
        assertNotNull(r);
        assertEquals(receiptable, r);
    }

    @Test
    void testClose() {
        handler.close();

        verify(receiptManager).clear();
    }

}
