package org.schlunzis.zis.stomp.client;

/// Exception thrown for errors related to message conversion in STOMP client.
///
/// This exception may be extended by custom message converters to indicate conversion failures.
///
/// @since 1.0.0
public class ConversionException extends RuntimeException {

    /// Constructs a new ConversionException with the specified detail message.
    ///
    /// @param message the detail message
    /// @since 1.0.0
    public ConversionException(String message) {
        super(message);
    }

    /// Constructs a new ConversionException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    /// @since 1.0.0
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Constructs a new ConversionException with the specified cause.
    ///
    /// @param cause the cause of the exception
    /// @since 1.0.0
    public ConversionException(Throwable cause) {
        super(cause);
    }

}
