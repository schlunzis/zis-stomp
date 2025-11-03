package org.schlunzis.zis.stomp.client;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/// A STOMP client for sending and receiving messages over the STOMP protocol.
///
/// This interface defines the core functionalities of a STOMP client, including connecting to a STOMP server,
/// sending messages to destinations, subscribing to destinations to receive messages, and unsubscribing from
/// subscriptions.
///
/// To create an instance of a STOMP client, use the [#builder()] method to obtain a [StompClientBuilder].
/// This builder allows you to configure the client.
///
/// To disconnect and release resources, call the [#close()] method.
///
/// You may also consider using the [StompSubscriber] annotation to define classes that can be
/// registered as subscribers to STOMP destinations, and [StompPublisher] annotation to define publisher interfaces
/// that can be used to send messages to STOMP destinations.
///
/// StompClient implementations are required to be thread-safe.
///
/// @see StompSubscriber
/// @see StompPublisher
/// @since 1.0.0
public interface StompClient extends AutoCloseable {

    /// Creates a new [StompClientBuilder] for building a STOMP client.
    ///
    /// The builder must be provided with an endpoint URI before building the client.
    ///
    /// Builder instances are not thread-safe and should not be shared between threads.
    ///
    /// @return a new instance of [StompClientBuilder]
    /// @since 1.0.0
    static StompClientBuilder builder() {
        return new StompClientBuilder();
    }

    /// Connects to the STOMP server.
    ///
    /// This method establishes a connection to the STOMP server specified in the client's configuration.
    /// It blocks until the connection is successfully established or fails.
    ///
    /// If the method is called a second time on the same client instance, an [IllegalStateException] is thrown.
    ///
    /// If the thread is interrupted while waiting for the connection to be established,
    /// an [ConnectionException] is thrown. The interrupt status of the thread is preserved.
    ///
    /// @throws ConnectionException   if the connection fails
    /// @throws IllegalStateException if the client has already been connected before
    /// @since 1.0.0
    CompletableFuture<Void> connect() throws ConnectionException;

    /// Connects to the STOMP server using the provided login and passcode.
    ///
    /// This method establishes a connection to the STOMP server specified in the client's configuration,
    /// using the provided login and passcode for authentication.
    /// It blocks until the connection is successfully established or fails.
    ///
    /// If the method is called a second time on the same client instance, an [IllegalStateException] is thrown.
    ///
    /// If the thread is interrupted while waiting for the connection to be established,
    /// an [ConnectionException] is thrown. The interrupt status of the thread is preserved.
    ///
    /// @param login    the login username
    /// @param passcode the passcode (password)
    /// @throws ConnectionException   if the connection fails
    /// @throws IllegalStateException if the client has already been connected before
    /// @since 1.0.0
    CompletableFuture<Void> connect(String login, String passcode) throws ConnectionException;

    /// Sends a message to the specified destination with the given body. This method does not use the provided message
    /// converter. It sends the body directly as a string and indicates a content type of `text/plain;charset=UTF-8`.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param destination the destination to end the message to
    /// @param body        the body of the message
    /// @throws IllegalStateException if the client is not connected
    /// @throws SendException         if sending the message fails or the message cannot be encoded
    /// @since 1.0.0
    void send(String destination, String body) throws SendException;

    /// Sends a message to the specified destination with the given body. The body is converted to a string using the
    /// client's configured message converter.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param destination the destination to send the message to
    /// @param body        the body of the message
    /// @throws IllegalStateException if the client is not connected
    /// @throws SendException         if sending the message fails or the message cannot be encoded
    /// @since 1.0.0
    void send(String destination, Object body) throws SendException;

    /// Subscribes to the specified destination to receive messages of the given payload type.
    ///
    /// The provided message handler is invoked for each received message, with the message payload converted to
    /// the specified type.
    ///
    /// The returned [Subscription] can be used to unsubscribe from the destination using the
    /// [#unsubscribe(Subscription)] method.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param destination    the destination to subscribe to
    /// @param payloadType    the type of the message payload
    /// @param messageHandler the handler to process received messages
    /// @param <T>            the type of the message payload
    /// @return a [Subscription] representing the subscription
    /// @throws IllegalStateException if the client is not connected
    /// @since 1.0.0
    <T> Subscription subscribe(String destination, Class<T> payloadType, Consumer<T> messageHandler);

    /// Subscribes all methods annotated with [Topic] in the given subscriber object.
    /// You can unsubscribe all created subscriptions by calling [#unsubscribe(Object)] with the same subscriber
    /// instance. To provide more configuration, annotate the class with [StompSubscriber].
    ///
    /// After calling this method, the subscriber's annotated methods will be invoked for incoming messages
    /// on their respective topics.
    /// If you call this method multiple times with the same subscriber instance,
    /// an [IllegalStateException] is thrown.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param subscriber the subscriber object containing methods annotated with [Topic]
    /// @throws IllegalStateException if the client is not connected
    /// @since 1.0.0
    void subscribe(Object subscriber);

    /// Unsubscribes from the specified subscription.
    ///
    /// After calling this method, no more messages will be received for the given subscription.
    /// If the subscription is already unsubscribed, this method has no effect.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param subscription the subscription to unsubscribe from
    /// @throws IllegalStateException if the client is not connected
    /// @since 1.0.0
    void unsubscribe(Subscription subscription);

    /// Unsubscribes all subscriptions created from the given subscriber object.
    ///
    /// After calling this method, no more messages will be received for any subscriptions created
    /// from the specified subscriber.
    /// If there are no subscriptions for the given subscriber, this method has no effect.
    ///
    /// If the client is not connected, an [IllegalStateException] is thrown.
    ///
    /// @param subscriber the subscriber object whose subscriptions should be unsubscribed
    /// @throws IllegalStateException if the client is not connected
    /// @since 1.0.0
    void unsubscribe(Object subscriber);

    /// Closes the STOMP client and releases all associated resources.
    ///
    /// This method disconnects from the STOMP server if connected and cleans up any resources used by the client.
    /// After calling this method, the client instance should not be used anymore.
    ///
    /// Subscriptions created by this client are also unsubscribed and will no longer receive messages.
    ///
    /// @since 1.0.0
    void close();

    /// Returns the message converter used by the STOMP client for converting message payloads.
    ///
    /// @return the message converter
    /// @since 1.0.0
    MessageConverter getMessageConverter();

}
