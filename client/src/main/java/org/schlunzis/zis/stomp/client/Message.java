package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.Optional;

/// Represents a STOMP message with headers and a body.
///
/// @param <T> the type of the message body
/// @since 1.0.0
public sealed interface Message<T> permits Frame {

    /// Returns the headers of the message.
    ///
    /// @return the headers
    /// @since 1.0.0
    Headers headers();

    /// Returns the body of the message.
    /// If the Message Object has been received, the body has been converted using the configured MessageConverter.
    /// If the Message Object is given to the STOMP client for sending, the body will be converted using the configured
    /// MessageConverter.
    ///
    /// If the body is not present, an empty Optional is returned.
    ///
    /// @return the body
    /// @since 1.0.0
    Optional<T> body();

}
