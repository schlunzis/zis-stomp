package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/// Outbound channel handler for attaching receipts to outgoing STOMP frames.
///
/// This handler uses a [ReceiptManager] to attach receipts to outgoing STOMP frames
/// based on the configured receipt policy.
///
/// It also sets the receipt latch and timeout in the interaction context if a receipt is attached.
/// This is used to wait for the receipt from the server after sending the frame.
///
/// @see ReceiptManager
/// @see OutboundChannel
/// @see org.schlunzis.zis.stomp.client.internal.interaction.AbstractInteractionContext
public final class ReceiptOutboundChannelHandler extends AbstractOutboundChannelHandler {

    private final ReceiptManager receiptManager;

    public ReceiptOutboundChannelHandler(ReceiptManager receiptManager) {
        this.receiptManager = receiptManager;
    }

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        Optional<CountDownLatch> latch = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        latch.ifPresent(l -> {
            context.receiptLatch(l);
            context.receiptTimeout(receiptManager.receiptTimeout());
        });

        super.handle(frameBuilder, context);
    }

    @Override
    public void close() {
        receiptManager.clear();
        super.close();
    }

}
