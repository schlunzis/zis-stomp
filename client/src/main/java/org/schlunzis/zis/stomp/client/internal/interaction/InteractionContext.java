package org.schlunzis.zis.stomp.client.internal.interaction;

import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.SendContext;
import org.schlunzis.zis.stomp.client.SubscribeContext;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/// This internal interface defines the contract for interaction contexts used in STOMP client operations.
///
/// An interaction defines metadata and behavior for sending a STOMP frame.
/// Instances are passed to the [org.schlunzis.zis.stomp.client.StompClient] and passed to the
/// [org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundChannel].
///
/// This allows internal access to methods that should not be exposed in the public API.
/// It is implemented by subclasses of the [AbstractInteractionContext].
/// The public part of the API is defined by the [SendContext] and [SubscribeContext] interfaces.
///
/// @param <T> The type of the interaction context, allowing for fluent method chaining.
public sealed interface InteractionContext<T> permits AbstractInteractionContext,
        SendContext, SubscribeContext {

    /// Adds a header to the STOMP frame.
    ///
    /// @param key   The header key.
    /// @param value The header value.
    /// @return The interaction context for method chaining.
    T header(String key, String value);

    /// Retrieves additional headers to be included in the STOMP frame.
    ///
    /// @return the headers
    Headers headers();

    /// Sets the latch to be used for receipt acknowledgment.
    ///
    /// @param countDownLatch The latch to be counted down upon receipt.
    /// @see org.schlunzis.zis.stomp.client.internal.ReceiptManager
    /// @see org.schlunzis.zis.stomp.client.internal.channel.inbound.ReceiptInboundChannelHandler
    void receiptLatch(CountDownLatch countDownLatch);

    /// Sets the timeout duration for waiting for a receipt acknowledgment.
    ///
    /// @param duration The timeout duration.
    /// @see org.schlunzis.zis.stomp.client.internal.channel.inbound.ReceiptInboundChannelHandler
    void receiptTimeout(Duration duration);

    /// Awaits the completion of the interaction, typically by waiting for a receipt acknowledgment.
    ///
    /// @throws org.schlunzis.zis.stomp.client.ReceiptTimeoutException if the receipt is not received within the
    ///                                                                specified timeout.
    void awaitCompletion();

}
