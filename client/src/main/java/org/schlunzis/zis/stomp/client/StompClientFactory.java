package org.schlunzis.zis.stomp.client;

/// Factory class for creating STOMP clients.
///
/// @see StompClientBuilder#stompClientFactory(StompClientFactory)
/// @since 1.0.0
@FunctionalInterface
public interface StompClientFactory {

    /// Creates a new STOMP client based on the provided builder configuration.
    ///
    /// @param builder the STOMP client builder
    /// @return the created STOMP client
    /// @since 1.0.0
    StompClient create(StompClientBuilder builder);

}
