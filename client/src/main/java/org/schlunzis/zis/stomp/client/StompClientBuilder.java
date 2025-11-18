package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/// Builder for [StompClient] instances.
///
/// @see StompClient#builder()
/// @since 1.0.0
public class StompClientBuilder {

    @Nullable
    private URI endpoint;
    @Nullable
    private MessageConverter messageConverter;
    @Nullable
    private OnErrorConsumer onErrorConsumer;
    private Duration receiptTimeout = Duration.ofSeconds(10);
    private ReceiptPolicy receiptPolicy = ReceiptPolicy.none();

    /// Creates a new STOMP client builder.
    ///
    /// @see StompClient#builder()
    /// @since 1.0.0
    StompClientBuilder() {

    }

    /// Sets the STOMP endpoint URI. This parameter is required.
    ///
    /// The protocol must be either `ws` or `wss`. Example: `ws://localhost:8080/ws`
    ///
    /// @param endpoint the STOMP endpoint URI
    /// @return the builder instance
    /// @since 1.0.0
    public StompClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public @Nullable URI endpoint() {
        return endpoint;
    }

    /// Sets the message converter to be used by the client. If not set, the builder will attempt to
    /// create a suitable MessageConverter automatically.
    ///
    /// It will first look for a Jackson 3 ObjectMapper, then for a Jackson 2 ObjectMapper. If one of them is found,
    /// a corresponding MessageConverter will be created. If none is found, a [StringMessageConverter] will be
    /// used.
    ///
    /// @param messageConverter the message converter
    /// @return the builder instance
    /// @since 1.0.0
    public StompClientBuilder messageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
        return this;
    }

    public @Nullable MessageConverter messageConverter() {
        return messageConverter;
    }

    /// Sets the consumer to run when a STOMP ERROR frame is received.
    /// If an ERROR frame is received from the server, the provided consumer will be invoked
    /// with the error details.
    ///
    /// If this happens, the server closes the connection, since ERROR frames are only sent
    /// in fatal error situations like protocol violations. If you think this is a problem
    /// with the client, please open an issue on GitHub.
    ///
    /// If you want to recover from such errors, you need to build a new StompClient instance
    /// and connect again. However, be aware that this is not recommended, since ERROR frames
    /// usually indicate serious problems.
    /// You can reuse the same builder instance to build a new client with the same configuration.
    ///
    /// The builder and the constructed StompClient will hold a strong reference to the provided
    /// consumer even after the client is closed.
    ///
    /// @param onErrorConsumer the error consumer
    /// @return the builder instance
    /// @since 1.0.0
    public StompClientBuilder onError(OnErrorConsumer onErrorConsumer) {
        Objects.requireNonNull(onErrorConsumer, "onErrorConsumer must not be null");
        this.onErrorConsumer = onErrorConsumer;
        return this;
    }

    public @Nullable OnErrorConsumer onErrorConsumer() {
        return onErrorConsumer;
    }

    /// Sets the receipt timeout duration.
    /// If a receipt is requested, this timeout defines how long the client
    /// will wait for the receipt frame from the server before considering it a failure.
    /// The default is 10 seconds.
    ///
    /// @param receiptTimeout the receipt timeout duration
    /// @return the builder instance
    /// @since 1.0.0
    public StompClientBuilder receiptTimeout(Duration receiptTimeout) {
        Objects.requireNonNull(receiptTimeout, "receiptTimeout must not be null");
        this.receiptTimeout = receiptTimeout;
        return this;
    }

    public Duration receiptTimeout() {
        return receiptTimeout;
    }

    /// Sets the receipt policy for the client.
    /// The receipt policy defines for which operations the client will request receipts from the server.
    /// By default, no receipts are requested.
    ///
    /// @param receiptPolicy the receipt policy
    /// @return the builder instance
    /// @since 1.0.0
    public StompClientBuilder receiptPolicy(ReceiptPolicy receiptPolicy) {
        Objects.requireNonNull(receiptPolicy, "receiptPolicy must not be null");
        this.receiptPolicy = receiptPolicy;
        return this;
    }

    public ReceiptPolicy receiptPolicy() {
        return receiptPolicy;
    }

    /// Builds the [StompClient] instance.
    ///
    /// You may call this method multiple times to create multiple clients with the same configuration.
    ///
    /// @return the STOMP client
    /// @throws IllegalStateException if the endpoint is not set
    /// @since 1.0.0
    public StompClient build() throws IllegalStateException {
        return new StompClientFactory().create(this);
    }

}
