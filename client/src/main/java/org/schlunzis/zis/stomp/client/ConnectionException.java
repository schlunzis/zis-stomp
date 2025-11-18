package org.schlunzis.zis.stomp.client;

/// Exception thrown if an error occurs while connecting to the STOMP server.
///
/// @since 1.0.0
public class ConnectionException extends RuntimeException {

    /// Constructs a new ConnectionException with the specified detail message.
    ///
    /// @param message the detail message
    /// @since 1.0.0
    public ConnectionException(String message) {
        super(message);
    }

    /// Constructs a new ConnectionException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param cause   the cause of the exception
    /// @since 1.0.0
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /// Constructs a new ConnectionException with the specified cause.
    ///
    /// @param cause the cause of the exception
    /// @since 1.0.0
    public ConnectionException(Throwable cause) {
        super(cause);
    }

}
