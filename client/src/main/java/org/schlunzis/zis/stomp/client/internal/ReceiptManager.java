package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.ReceiptTimeoutException;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

final class ReceiptManager {

    private static final Logger log = LoggerFactory.getLogger(ReceiptManager.class);

    private final WebSocketClient websocketClient;

    private final Map<UUID, CountDownLatch> receiptLatches = new ConcurrentHashMap<>();
    private final Duration receiptTimeout;
    private final ReceiptPolicy receiptPolicy;

    ReceiptManager(WebSocketClient websocketClient, Duration receiptTimeout, ReceiptPolicy receiptPolicy) {
        this.websocketClient = websocketClient;
        this.receiptTimeout = receiptTimeout;
        this.receiptPolicy = receiptPolicy;
    }

    /**
     * Sends a frame and awaits a receipt if the policy requires it.
     *
     * @param frame  the frame to send
     * @param policy the receipt policy to check
     */
    void sendAndAwaitReceiptIfPolicy(Frame frame, ReceiptPolicy.Policy policy) {
        if (receiptPolicy.isEnabled(policy)) {
            sendAndAwaitReceipt(frame);
        } else {
            websocketClient.send(frame);
        }
    }

    /**
     * Sends a frame and waits for the corresponding RECEIPT frame.
     *
     * @param frame the frame to send
     * @throws SendException if the receipt is not received within the timeout
     */
    void sendAndAwaitReceipt(Frame frame) {
        UUID receiptId = UUID.randomUUID();
        frame.headers().addFirst("receipt", receiptId.toString());
        CountDownLatch latch = new CountDownLatch(1);
        receiptLatches.put(receiptId, latch);
        websocketClient.send(frame);
        try {
            if (!latch.await(receiptTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw new ReceiptTimeoutException("Did not receive receipt for id " + receiptId + " within " + receiptTimeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SendException("Interrupted while waiting for receipt", e);
        } finally {
            receiptLatches.remove(receiptId);
        }
    }

    /**
     * Handles an incoming RECEIPT frame.
     * It looks up the corresponding latch and counts it down.
     *
     * @param frame the RECEIPT frame
     */
    void handleReceipt(Frame frame) {
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

    public void clear() {
        receiptLatches.clear();
    }

}
