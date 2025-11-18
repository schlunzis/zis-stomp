package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.SendContextImpl;

/// Context for sending a STOMP message.
///
/// This can be obtained via the [StompClient#send(SendContext)] method.
///
/// @see StompClient#send(SendContext)
/// @since 1.0.0
public non-sealed interface SendContext extends InteractionContext<SendContext> {

    static SendContext create(String destination, Object body) {
        return new SendContextImpl(destination, body);
    }

    /// Adds a header to the STOMP message to be sent.
    ///
    /// This may be a custom header or a standard STOMP header.
    /// Headers set by default by the client (e.g., `destination` or `content-type`) may be overridden.
    ///
    /// @param key   the header key
    /// @param value the header value
    /// @return the current [SendContext] for method chaining
    /// @throws NullPointerException if key or value is `null`
    /// @since 1.0.0
    SendContext header(String key, String value);

    Headers headers();

    String destination();

    Object body();

}
