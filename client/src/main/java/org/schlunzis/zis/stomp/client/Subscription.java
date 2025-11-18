package org.schlunzis.zis.stomp.client;

import java.util.UUID;
import java.util.function.Consumer;

/// Represents a STOMP subscription.
///
/// @see StompClient#subscribe(String, Class, Consumer)
/// @see StompClient#unsubscribe(Subscription)
/// @since 1.0.0
public interface Subscription {

    /// The unique identifier of the subscription.
    ///
    /// @return the subscription ID
    /// @since 1.0.0
    UUID id();

    /// The destination (topic) of the subscription.
    ///
    /// @return the destination string
    /// @since 1.0.0
    String destination();

}
