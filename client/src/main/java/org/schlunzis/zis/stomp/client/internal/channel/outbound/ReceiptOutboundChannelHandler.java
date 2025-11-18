package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class ReceiptOutboundChannelHandler extends AbstractOutboundChannelHandler {

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
