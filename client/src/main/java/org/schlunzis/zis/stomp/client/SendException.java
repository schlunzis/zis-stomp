package org.schlunzis.zis.stomp.client;

/// Exception thrown for errors related to sending STOMP messages.
///
/// @since 1.0.0
public class SendException extends RuntimeException {

    /// Constructs a new SendException with the specified detail message.
    ///
    /// @param message the detail message
    /// @since 1.0.0
    public SendException(String message) {
        super(message);
    }

    /// Constructs a new SendException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    /// @since 1.0.0
    public SendException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Constructs a new SendException with the specified cause.
    ///
    /// @param cause the cause of the exception
    /// @since 1.0.0
    public SendException(Throwable cause) {
        super(cause);
    }

}
