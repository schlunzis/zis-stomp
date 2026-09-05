package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.SendContextImpl;
import org.schlunzis.zis.stomp.common.Headers;

/// Context for sending a STOMP message.
///
/// An instance of this interface can be obtained via [SendContext#create(String, Object)].
/// It allows setting various parameters for the STOMP message to be sent, such as headers.
/// After its configuration, the [SendContext] can be passed to the [StompClient#send(SendContext)] method to send the
/// message.
///
/// @see StompClient#send(SendContext)
/// @since 1.0.0
public non-sealed interface SendContext extends InteractionContext<SendContext> {

    /// Creates a new [SendContext] for sending a STOMP message.
    ///
    /// Required parameters are passed here to make sure they are always set.
    /// Optional parameters can be set via the methods of the returned instance.
    ///
    /// @param destination the destination to which the message will be sent
    /// @param body        the body of the message
    /// @return a new [SendContext] instance
    static SendContext create(String destination, Object body) {
        return new SendContextImpl(destination, body);
    }

    /// Adds a header to the STOMP message to be sent.
    ///
    /// This may be a custom header or a standard STOMP header.
    /// Headers set by default by the client (e.g., `destination` or `content-type`) may be overridden.
    /// Keep in mind that some headers are mandatory for certain STOMP commands and must be set
    /// correctly to ensure proper communication with the STOMP server.
    ///
    /// @param key   the header key
    /// @param value the header value
    /// @return the current [SendContext] for method chaining
    /// @throws NullPointerException if key or value is `null`
    /// @since 1.0.0
    SendContext header(String key, String value);

    /// Returns the custom headers set in this context.
    ///
    /// @return the headers
    /// @since 1.0.0
    Headers headers();

    /// Returns the destination to which the STOMP message will be sent.
    ///
    /// @return the destination
    /// @since 1.0.0
    String destination();

    /// Returns the body of the STOMP message to be sent.
    ///
    /// @return the body
    /// @since 1.0.0
    Object body();

}
