package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.Stomp1dot2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Builder for {@link StompClient}.
 *
 * @see StompClient#builder()
 * @since 1.0.0
 */
public final class StompClientBuilder {

    private static final Logger log = LoggerFactory.getLogger(StompClientBuilder.class);

    @Nullable
    private URI endpoint;
    @Nullable
    private MessageConverter messageConverter;
    @Nullable
    private OnErrorConsumer onErrorConsumer;
    private Duration receiptTimeout = Duration.ofSeconds(10);
    private ReceiptPolicy receiptPolicy = ReceiptPolicy.none();

    /**
     * Creates a new STOMP client builder.
     *
     * @see StompClient#builder()
     * @since 1.0.0
     */
    StompClientBuilder() {

    }

    /**
     * Sets the STOMP endpoint URI. This parameter is required.
     * <p>
     * The protocol must be either "ws" or "wss".
     *
     * @param endpoint the STOMP endpoint URI
     * @return the builder instance
     * @since 1.0.0
     */
    public StompClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the message converter to be used by the client. If not set, the builder will attempt to
     * find a suitable message converter via a ServiceLoader.
     * <p>
     * It will first look for a Jackson 3 ObjectMapper, then for a Jackson 2 ObjectMapper. If one of them is found,
     * a corresponding MessageConverter will be created. If none is found, a {@link StringMessageConverter} will be
     * used.
     *
     * @param messageConverter the message converter
     * @return the builder instance
     * @since 1.0.0
     */
    public StompClientBuilder messageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
        return this;
    }

    /**
     * Sets the consumer to run when a STOMP ERROR frame is received.
     * If an ERROR frame is received from the server, the provided consumer will be invoked
     * with the error details.
     * <p>
     * If this happens, the server closes the connection, since ERROR frames are only sent
     * in fatal error situations like protocol violations. If you think this is a problem
     * with the client, please open an issue on GitHub.
     * <p>
     * If you want to recover from such errors, you need to build a new StompClient instance
     * and connect again. However, be aware that this is not recommended, since ERROR frames
     * usually indicate serious problems.
     * You can reuse the same builder instance to build a new client with the same configuration.
     * <p>
     * The builder and the constructed StompClient will hold a strong reference to the provided
     * consumer even after the client is closed.
     *
     * @param onErrorConsumer the error consumer
     * @return the builder instance
     * @since 1.0.0
     */
    public StompClientBuilder onError(OnErrorConsumer onErrorConsumer) {
        Objects.requireNonNull(onErrorConsumer, "onErrorConsumer must not be null");
        this.onErrorConsumer = onErrorConsumer;
        return this;
    }

    /**
     * Sets the receipt timeout duration.
     * If a receipt is requested, this timeout defines how long the client
     * will wait for the receipt frame from the server before considering it a failure.
     * The default is 10 seconds.
     *
     * @param receiptTimeout the receipt timeout duration
     * @return the builder instance
     * @since 1.0.0
     */
    public StompClientBuilder receiptTimeout(Duration receiptTimeout) {
        Objects.requireNonNull(receiptTimeout, "receiptTimeout must not be null");
        this.receiptTimeout = receiptTimeout;
        return this;
    }

    /**
     * Sets the receipt policy for the client.
     * The receipt policy defines for which operations the client will request receipts from the server.
     * By default, no receipts are requested.
     *
     * @param receiptPolicy the receipt policy
     * @return the builder instance
     * @since 1.0.0
     */
    public StompClientBuilder receiptPolicy(ReceiptPolicy receiptPolicy) {
        Objects.requireNonNull(receiptPolicy, "receiptPolicy must not be null");
        this.receiptPolicy = receiptPolicy;
        return this;
    }

    /**
     * Builds the {@link StompClient} instance.
     * <p>
     * You may call this method multiple times to create multiple clients with the same configuration.
     *
     * @return the STOMP client
     * @throws IllegalStateException if the endpoint is not set
     * @since 1.0.0
     */
    public StompClient build() throws IllegalStateException {
        if (endpoint == null) {
            throw new IllegalStateException("Endpoint must be set");
        }
        if (messageConverter == null) {
            messageConverter = findMessageConverter();
        }

        return new Stomp1dot2Client(
                endpoint,
                messageConverter,
                onErrorConsumer,
                receiptTimeout,
                receiptPolicy
        );
    }

    private MessageConverter findMessageConverter() {
        Class<?> mapperClass;
        boolean jackson3 = false;
        try {
            mapperClass = Class.forName("tools.jackson.databind.ObjectMapper");
            jackson3 = true;
            log.debug("Found Jackson 3 ObjectMapper");
        } catch (ClassNotFoundException _) {
            try {
                mapperClass = Class.forName("com.fasterxml.jackson.core.ObjectCodec");
                log.debug("Found Jackson 2 ObjectMapper");
            } catch (ClassNotFoundException _) {
                log.debug("No Jackson ObjectMapper found");
                return new StringMessageConverter();
            }
        }

        ServiceLoader<?> loader = ServiceLoader.load(mapperClass);
        Optional<?> mapper = loader.findFirst();
        if (mapper.isPresent()) {
            log.debug("Found {} via ServiceLoader", mapperClass);
            if (jackson3)
                return new Jackson3MessageConverter((tools.jackson.databind.ObjectMapper) mapper.get());
            else
                return new Jackson2MessageConverter((com.fasterxml.jackson.databind.ObjectMapper) mapper.get());
        }

        log.debug("No MessageConverter found via ServiceLoader");
        return new StringMessageConverter();
    }

}
