package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiptInboundChannelHandler extends AbstractInboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceiptInboundChannelHandler.class);
    private final ReceiptManager receiptManager;

    public ReceiptInboundChannelHandler(ReceiptManager receiptManager) {
        this.receiptManager = receiptManager;
    }

    @Override
    public void handle(Frame frame) {
        log.trace("Handling frame for RECEIPT {}", frame);
        if (Command.RECEIPT.equals(frame.command())) {
            receiptManager.handleReceipt(frame);
        }

        super.handle(frame);
    }

    @Override
    public void close() {
        receiptManager.clear();
        super.close();
    }
}
