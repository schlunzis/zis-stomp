package org.schlunzis.zis.stomp.client;

/// Exception thrown when a receipt is not received within the configured timeout period.
///
/// @since 1.0.0
public final class ReceiptTimeoutException extends RuntimeException {

    /// Constructs a new ReceiptTimeoutException with the specified detail message.
    ///
    /// @param message the detail message
    /// @since 1.0.0
    public ReceiptTimeoutException(String message) {
        super(message);
    }

    /// Constructs a new ReceiptTimeoutException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    /// @since 1.0.0
    public ReceiptTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Constructs a new ReceiptTimeoutException with the specified cause.
    ///
    /// @param cause the cause of the exception
    /// @since 1.0.0
    public ReceiptTimeoutException(Throwable cause) {
        super(cause);
    }

}
