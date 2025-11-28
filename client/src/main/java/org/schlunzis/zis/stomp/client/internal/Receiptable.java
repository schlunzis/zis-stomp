package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.ReceiptTimeoutException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/// A record representing a receiptable entity with a timeout and a latch.
///
/// @see ReceiptManager
/// @see ReceiptTimeoutException
/// @see org.schlunzis.zis.stomp.client.internal.channel.outbound.ReceiptOutboundChannelHandler
/// @see org.schlunzis.zis.stomp.client.internal.channel.inbound.ReceiptInboundChannelHandler
public record Receiptable(
        Duration timeout,
        CountDownLatch latch
) {

    /// Signals that the receipt has been received by counting down the latch.
    public void signal() {
        latch.countDown();
    }

    /// Awaits the receipt acknowledgment within the specified timeout.
    ///
    /// @throws ReceiptTimeoutException if the receipt is not received within the timeout duration
    public void await() throws InterruptedException {
        if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new ReceiptTimeoutException("Receipt not received within time: " + timeout);
        }
    }

}
