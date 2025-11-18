package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class ReceiptOutboundChannelHandler extends AbstractOutboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceiptOutboundChannelHandler.class);
    private final ReceiptManager receiptManager;

    public ReceiptOutboundChannelHandler(ReceiptManager receiptManager) {
        this.receiptManager = receiptManager;
    }

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        log.trace("Adding receipt header if enabled to outbound frame {}", frameBuilder);
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
