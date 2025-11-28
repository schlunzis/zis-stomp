package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReceiptInboundChannelHandlerTest {

    @Mock
    ReceiptManager receiptManager;

    @Test
    void testHandle() {
        ReceiptInboundChannelHandler handler = new ReceiptInboundChannelHandler(receiptManager);
        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                .build();

        handler.handle(receiptFrame);

        verify(receiptManager).handleReceipt(receiptFrame);
    }

    @Test
    void testHandleNonReceiptFrame() {
        ReceiptInboundChannelHandler handler = new ReceiptInboundChannelHandler(receiptManager);
        Frame messageFrame = Frame.builder()
                .command(Command.MESSAGE)
                .build();

        handler.handle(messageFrame);

        verify(receiptManager, never()).handleReceipt(messageFrame);
    }

    @Test
    void testClose() {
        ReceiptInboundChannelHandler handler = new ReceiptInboundChannelHandler(receiptManager);
        handler.close();
        verify(receiptManager).clear();
    }

}
