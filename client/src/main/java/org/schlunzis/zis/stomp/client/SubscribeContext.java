package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.SubscribeContextImpl;
import org.schlunzis.zis.stomp.common.Headers;

import java.util.function.Consumer;

/// Context for subscribing to a STOMP destination.
///
/// This can be obtained via the [StompClient#subscribe(SubscribeContext)] method.
///
/// @param <T> the type of the payload expected in the messages received from the subscription
/// @see StompClient#subscribe(SubscribeContext)
/// @since 1.0.0
public non-sealed interface SubscribeContext<T> extends InteractionContext<SubscribeContext<T>> {

    /// Creates a new [SubscribeContext] for subscribing to a STOMP destination.
    ///
    /// Required parameters are passed here to make sure they are always set.
    /// Optional parameters can be set via the methods of the returned instance.
    ///
    /// @param destination    the destination to which to subscribe
    /// @param payloadType    the expected type of the message payloads received from the subscription
    /// @param messageHandler the handler to process received messages
    /// @param <T>            the type of the payload expected in the messages received from the subscription
    /// @return a new [SubscribeContext] instance
    static <T> SubscribeContext<T> create(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        return new SubscribeContextImpl<>(
                destination,
                payloadType,
                messageHandler
        );
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
    SubscribeContext<T> header(String key, String value);

    /// Returns the custom headers set in this context.
    ///
    /// @return the headers
    /// @since 1.0.0
    Headers headers();

    /// Returns the destination to which the STOMP subscription will be made.
    ///
    /// @return the destination
    /// @since 1.0.0
    String destination();

    /// Returns the expected type of the message payloads received from the subscription.
    ///
    /// @return the payload type
    /// @since 1.0.0
    Class<T> payloadType();

    /// Returns the handler to process received messages.
    ///
    /// @return the message handler
    /// @since 1.0.0
    Consumer<T> messageHandler();

}
