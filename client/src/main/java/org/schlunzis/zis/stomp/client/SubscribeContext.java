package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.SubscribeContextImpl;

import java.util.function.Consumer;

/// Context for subscribing to a STOMP destination.
///
/// This can be obtained via the [StompClient#subscribe(SubscribeContext)] method.
///
/// @see StompClient#subscribe(SubscribeContext)
/// @since 1.0.0
public non-sealed interface SubscribeContext<T> extends InteractionContext<SubscribeContext<T>> {

    static <T> SubscribeContext<T> create(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        return new SubscribeContextImpl<>(
                destination,
                payloadType,
                messageHandler
        );
    }

    /// Adds a header to the STOMP message to be sent.
    ///
    /// This may be a custom header or a standard STOMP header.
    /// Headers set by default by the client (e.g., `destination` or `content-type`) may be overridden.
    ///
    /// @param key   the header key
    /// @param value the header value
    /// @return the current [SubscribeContext] for method chaining
    /// @throws NullPointerException if key or value is `null`
    /// @since 1.0.0
    SubscribeContext<T> header(String key, String value);

    String destination();

    Class<T> payloadType();

    Consumer<T> messageHandler();

}
