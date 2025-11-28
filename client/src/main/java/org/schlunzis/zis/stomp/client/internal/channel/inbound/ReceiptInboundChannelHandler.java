package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

/// Inbound channel handler for processing RECEIPT frames.
///
/// Frames with the RECEIPT command are passed to the [ReceiptManager] for handling.
///
/// @see ReceiptManager
public final class ReceiptInboundChannelHandler extends AbstractInboundChannelHandler {

    private final ReceiptManager receiptManager;

    public ReceiptInboundChannelHandler(ReceiptManager receiptManager) {
        this.receiptManager = receiptManager;
    }

    @Override
    public void handle(Frame frame) {
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
