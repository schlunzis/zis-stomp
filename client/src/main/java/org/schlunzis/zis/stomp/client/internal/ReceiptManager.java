package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public final class ReceiptManager {

    private static final Logger log = LoggerFactory.getLogger(ReceiptManager.class);

    private final Map<UUID, CountDownLatch> receiptLatches = new ConcurrentHashMap<>();
    private final Duration receiptTimeout;
    private final ReceiptPolicy receiptPolicy;

    public ReceiptManager(Duration receiptTimeout, ReceiptPolicy receiptPolicy) {
        this.receiptTimeout = receiptTimeout;
        this.receiptPolicy = receiptPolicy;
    }

    public Optional<CountDownLatch> attachReceiptIfPolicyEnabled(FrameBuilder frameBuilder) {
        Command command = frameBuilder.command();
        return switch (command) {
            case SEND -> {
                if (receiptPolicy.isEnabled(ReceiptPolicy.Policy.FOR_SEND))
                    yield Optional.of(attachReceipt(frameBuilder));
                else
                    yield Optional.empty();
            }
            case SUBSCRIBE -> {
                if (receiptPolicy.isEnabled(ReceiptPolicy.Policy.FOR_SUBSCRIBE))
                    yield Optional.of(attachReceipt(frameBuilder));
                else
                    yield Optional.empty();
            }
            case UNSUBSCRIBE -> {
                if (receiptPolicy.isEnabled(ReceiptPolicy.Policy.FOR_UNSUBSCRIBE))
                    yield Optional.of(attachReceipt(frameBuilder));
                else
                    yield Optional.empty();
            }
            case DISCONNECT -> {
                if (receiptPolicy.isEnabled(ReceiptPolicy.Policy.FOR_DISCONNECT))
                    yield Optional.of(attachReceipt(frameBuilder));
                else
                    yield Optional.empty();
            }
            default -> Optional.empty();
        };
    }

    private CountDownLatch attachReceipt(FrameBuilder frameBuilder) {
        UUID receiptId = UUID.randomUUID();
        frameBuilder.header("receipt", receiptId.toString());
        CountDownLatch latch = new CountDownLatch(1);
        receiptLatches.put(receiptId, latch);
        return latch;
    }

    /// Handles an incoming `RECEIPT` frame.
    /// It looks up the corresponding latch and counts it down.
    ///
    /// @param frame the `RECEIPT` frame
    public void handleReceipt(Frame frame) {
        UUID receiptId;
        try {
            receiptId = UUID.fromString(frame.headers().get("receipt-id").getFirst());
        } catch (IllegalArgumentException _) {
            log.warn("Received RECEIPT with invalid receipt id: {}", frame);
            return;
        }

        CountDownLatch latch = receiptLatches.get(receiptId);
        if (latch != null) {
            latch.countDown();
        } else {
            log.warn("Received RECEIPT for unknown receipt id: {}", receiptId);
        }
    }

    public Duration receiptTimeout() {
        return receiptTimeout;
    }

    public void clear() {
        receiptLatches.clear();
    }

}
