package org.schlunzis.zis.stomp.client;

/// Context for sending a STOMP message.
///
/// This can be obtained via the [StompClient#sendWith(String, Object)] method.
///
/// @see StompClient#sendWith(String, Object)
/// @since 1.0.0
public interface SendContext {

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

    /// Sends the STOMP message configured in this [SendContext].
    ///
    /// @throws SendException if sending the message fails or the message cannot be encoded
    /// @since 1.0.0
    void send() throws SendException;

}
