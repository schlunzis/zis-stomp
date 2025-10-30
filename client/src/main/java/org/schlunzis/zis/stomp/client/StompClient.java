package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.Stomp1dot2Client;

import java.util.function.Consumer;

/**
 * A STOMP client for sending and receiving messages over the STOMP protocol.
 * <p>
 * This interface defines the core functionalities of a STOMP client, including connecting to a STOMP server,
 * sending messages to destinations, subscribing to destinations to receive messages, and unsubscribing from
 * subscriptions.
 * <p>
 * To create an instance of a STOMP client, use the {@link #builder()} method to obtain a {@link StompClientBuilder}.
 * To disconnect and release resources, call the {@link #close()} method.
 * <p>
 * You may also consider using the {@link StompSubscriber} annotation to define classes that can be
 * registered as subscribers to STOMP destinations, and {@link StompPublisher} annotation to define publisher interfaces
 * that can be used to send messages to STOMP destinations.
 *
 * @see StompSubscriber
 * @see StompPublisher
 * @since 1.0.0
 */
public sealed interface StompClient
        extends AutoCloseable
        permits Stomp1dot2Client {

    /**
     * Creates a new {@link StompClientBuilder} for building a STOMP client.
     * <p>
     * The builder must be provided with an endpoint URI before building the client.
     *
     * @return a new instance of {@link StompClientBuilder}
     * @since 1.0.0
     */
    static StompClientBuilder builder() {
        return new StompClientBuilder();
    }

    /**
     * Connects to the STOMP server.
     * <p>
     * This method establishes a connection to the STOMP server specified in the client's configuration.
     * It blocks until the connection is successfully established or fails.
     * <p>
     * If the method is called a second time on the same client instance, an {@link IllegalStateException} is thrown.
     *
     * @throws ConnectionException   if the connection fails
     * @throws IllegalStateException if the client has already been connected before
     * @since 1.0.0
     */
    void connect() throws ConnectionException;

    /**
     * Sends a message to the specified destination with the given body. This method does not use the provided message
     * converter. It sends the body directly as a string and indicates a content type of "text/plain;charset=UTF-8".
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param destination the destination to end the message to
     * @param body        the body of the message
     * @throws IllegalStateException if the client is not connected
     * @throws SendException         if sending the message fails or the message cannot be encoded
     * @since 1.0.0
     */
    void send(String destination, String body) throws SendException;

    /**
     * Sends a message to the specified destination with the given body. The body is converted to a string using the
     * client's configured message converter.
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param destination the destination to send the message to
     * @param body        the body of the message
     * @throws IllegalStateException if the client is not connected
     * @throws SendException         if sending the message fails or the message cannot be encoded
     * @since 1.0.0
     */
    void send(String destination, Object body) throws SendException;

    /**
     * Subscribes to the specified destination to receive messages of the given payload type.
     * <p>
     * The provided message handler is invoked for each received message, with the message payload converted to
     * the specified type.
     * <p>
     * The returned {@link Subscription} can be used to unsubscribe from the destination using the
     * {@link #unsubscribe(Subscription)} method.
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param destination    the destination to subscribe to
     * @param payloadType    the type of the message payload
     * @param messageHandler the handler to process received messages
     * @param <T>            the type of the message payload
     * @return a {@link Subscription} representing the subscription
     * @throws IllegalStateException if the client is not connected
     * @since 1.0.0
     */
    <T> Subscription subscribe(String destination, Class<T> payloadType, Consumer<T> messageHandler);

    /**
     * Subscribes all methods annotated with {@link Topic} in the given subscriber object.
     * You can unsubscribe all created subscriptions by calling {@link #unsubscribe(Object)} with the same subscriber
     * instance. To provide more configuration, annotate the class with {@link StompSubscriber}.
     * <p>
     * After calling this method, the subscriber's annotated methods will be invoked for incoming messages
     * on their respective topics.
     * If you call this method multiple times with the same subscriber instance,
     * an {@link IllegalStateException} is thrown.
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param subscriber the subscriber object containing methods annotated with {@link Topic}
     * @throws IllegalStateException if the client is not connected
     * @since 1.0.0
     */
    void subscribe(Object subscriber);

    /**
     * Unsubscribes from the specified subscription.
     * <p>
     * After calling this method, no more messages will be received for the given subscription.
     * If the subscription is already unsubscribed, this method has no effect.
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param subscription the subscription to unsubscribe from
     * @throws IllegalStateException if the client is not connected
     * @since 1.0.0
     */
    void unsubscribe(Subscription subscription);

    /**
     * Unsubscribes all subscriptions created from the given subscriber object.
     * <p>
     * After calling this method, no more messages will be received for any subscriptions created
     * from the specified subscriber.
     * If there are no subscriptions for the given subscriber, this method has no effect.
     * <p>
     * If the client is not connected, an {@link IllegalStateException} is thrown.
     *
     * @param subscriber the subscriber object whose subscriptions should be unsubscribed
     * @throws IllegalStateException if the client is not connected
     * @since 1.0.0
     */
    void unsubscribe(Object subscriber);

    /**
     * Closes the STOMP client and releases all associated resources.
     * <p>
     * This method disconnects from the STOMP server if connected and cleans up any resources used by the client.
     * After calling this method, the client instance should not be used anymore.
     *
     * @since 1.0.0
     */
    void close();

    /**
     * Returns the message converter used by the STOMP client for converting message payloads.
     *
     * @return the message converter
     * @since 1.0.0
     */
    MessageConverter getMessageConverter();

}
