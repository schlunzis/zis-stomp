package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.StompClientBuilder;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/// This class manages the attachment and handling of receipts for STOMP frames.
///
/// A receipt id produced by this manager is always a UUID.
/// When a frame should be sent with a receipt request, a unique receipt id is generated,
/// attached to the frame, and a CountDownLatch is created and stored in a map with the receipt id as the key.
/// When a RECEIPT frame is received, the manager looks up the corresponding latch and counts it down,
/// allowing the sender to proceed.
///
/// Weather or not a receipt should be requested for a frame is determined by the configured ReceiptPolicy.
///
/// @see ReceiptPolicy
/// @see StompClientBuilder#receiptPolicy()
public final class ReceiptManager {

    private static final Logger log = LoggerFactory.getLogger(ReceiptManager.class);

    private final Map<UUID, Receiptable> receiptables = new ConcurrentHashMap<>();
    private final Duration receiptTimeout;
    private final ReceiptPolicy receiptPolicy;

    public ReceiptManager(Duration receiptTimeout, ReceiptPolicy receiptPolicy) {
        Objects.requireNonNull(receiptTimeout, "receiptTimeout must not be null");
        Objects.requireNonNull(receiptPolicy, "receiptPolicy must not be null");
        this.receiptTimeout = receiptTimeout;
        this.receiptPolicy = receiptPolicy;
    }

    /// Attaches a receipt header to the given frame builder if the receipt policy
    /// is enabled for the command of the frame.
    ///
    /// @param frameBuilder the frame builder to attach the receipt header to
    /// @return an optional containing the CountDownLatch if a receipt was attached, or empty
    public Optional<Receiptable> attachReceiptIfPolicyEnabled(FrameBuilder frameBuilder) {
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

    private Receiptable attachReceipt(FrameBuilder frameBuilder) {
        UUID receiptId = UUID.randomUUID();
        frameBuilder.header("receipt", receiptId.toString());
        CountDownLatch latch = new CountDownLatch(1);
        Receiptable receiptable = new Receiptable(receiptTimeout, latch);
        receiptables.put(receiptId, receiptable);
        return receiptable;
    }

    /// Handles an incoming `RECEIPT` frame.
    /// It looks up the corresponding latch and counts it down.
    ///
    /// @param frame the `RECEIPT` frame
    /// @see org.schlunzis.zis.stomp.client.internal.channel.inbound.ReceiptInboundChannelHandler
    public void handleReceipt(Frame frame) {
        UUID receiptId;
        try {
            String stringId = Objects.requireNonNull(frame.headers().getFirst("receipt-id"));
            receiptId = UUID.fromString(stringId);
        } catch (IllegalArgumentException | NullPointerException _) {
            log.warn("Received RECEIPT with invalid receipt id: {}", frame);
            return;
        }

        Receiptable receiptable = receiptables.remove(receiptId);
        if (receiptable != null) {
            receiptable.signal();
        } else {
            log.warn("Received RECEIPT for unknown receipt id: {}", receiptId);
        }
    }

    /// Returns the receipt timeout duration.
    ///
    /// @return the receipt timeout duration
    /// @see StompClientBuilder#receiptTimeout()
    public Duration receiptTimeout() {
        return receiptTimeout;
    }

    /// Clears all resources held by the ReceiptManager.
    public void clear() {
        receiptables.clear();
    }

}
