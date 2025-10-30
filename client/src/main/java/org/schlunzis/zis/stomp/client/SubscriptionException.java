package org.schlunzis.zis.stomp.client;

/// Exception thrown for errors related to STOMP subscriptions.
///
/// @since 1.0.0
public final class SubscriptionException extends RuntimeException {

    /// Constructs a new SubscriptionException with the specified detail message.
    ///
    /// @param message the detail message
    /// @since 1.0.0
    public SubscriptionException(String message) {
        super(message);
    }

    /// Constructs a new SubscriptionException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    /// @since 1.0.0
    public SubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Constructs a new SubscriptionException with the specified cause.
    ///
    /// @param cause the cause of the exception
    /// @since 1.0.0
    public SubscriptionException(Throwable cause) {
        super(cause);
    }

}
