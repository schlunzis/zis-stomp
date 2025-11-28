package org.schlunzis.zis.stomp.client.internal.interaction;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.SendContext;
import org.schlunzis.zis.stomp.client.SubscribeContext;
import org.schlunzis.zis.stomp.client.internal.Receiptable;

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

    /// Sets the receiptable containing the latch and timeout for receipt acknowledgment.
    ///
    /// @param receiptable The receiptable.
    void receiptable(Receiptable receiptable);

    /// Retrieves the receiptable associated with this interaction, if any.
    ///
    /// @return The receiptable, or null if none is set.
    @Nullable
    Receiptable receiptable();

    /// Awaits the completion of the interaction, typically by waiting for a receipt acknowledgment.
    ///
    /// @throws org.schlunzis.zis.stomp.client.ReceiptTimeoutException if the receipt is not received within the
    void awaitCompletion();

}
