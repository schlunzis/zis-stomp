package org.schlunzis.zis.stomp.client;

import java.util.function.Consumer;

/// Context for subscribing to a STOMP destination.
///
/// This can be obtained via the [StompClient#subscribeWith(String, Class, Consumer)] method.
///
/// @see StompClient#subscribeWith(String, Class, Consumer)
/// @since 1.0.0
public interface SubscribeContext {

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
    SubscribeContext header(String key, String value);

    /// Subscribes to the destination with the additional configuration.
    ///
    /// @since 1.0.0
    Subscription subscribe();

}
