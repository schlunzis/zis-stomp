package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Builder for {@link StompClient}.
 */
public final class StompClientBuilder {

    private static final Logger log = LoggerFactory.getLogger(StompClientBuilder.class);
    @Nullable
    private URI endpoint;
    private final List<Object> subscribers = new ArrayList<>();
    @Nullable
    private MessageConverter messageConverter;

    StompClientBuilder() {

    }

    /**
     * Sets the STOMP endpoint URI. This parameter is required.
     * <p>
     * The protocol must be either "ws" or "wss".
     *
     * @param endpoint the STOMP endpoint URI
     * @return the builder instance
     */
    public StompClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Adds a subscriber instance to the client. This instance must be annotated with
     * {@link StompSubscriber} and its methods must be annotated with {@link Topic}.
     * <p>
     * This method may be called multiple times to add multiple subscribers.
     *
     * @param subscribers the subscriber instances
     * @return the builder instance
     */
    public StompClientBuilder subscribers(List<Object> subscribers) {
        this.subscribers.addAll(subscribers);
        return this;
    }

    /**
     * Sets the message converter to be used by the client. If not set, the builder will attempt to
     * find a suitable message converter via ServiceLoader.
     * <p>
     * It will first look for Jackson 3's ObjectMapper, then for Jackson 2's ObjectMapper. If one of them is found,
     * a corresponding MessageConverter will be created. If none is found, a {@link StringMessageConverter} will be
     * used.
     *
     * @param messageConverter the message converter
     * @return the builder instance
     */
    public StompClientBuilder messageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
        return this;
    }

    /**
     * Builds the {@link StompClient} instance.
     *
     * @return the STOMP client
     * @throws IllegalStateException if the endpoint is not set
     */
    public StompClient build() {
        if (endpoint == null) {
            throw new IllegalStateException("Endpoint must be set");
        }
        if (messageConverter == null) {
            messageConverter = findMessageConverter();
        }

        return new Stomp1dot2Client(
                endpoint,
                subscribers,
                messageConverter
        );
    }

    private MessageConverter findMessageConverter() {
        Class<?> mapperClass;
        boolean jackson3 = false;
        try {
            mapperClass = Class.forName("tools.jackson.databind.ObjectMapper");
            jackson3 = true;
            log.debug("Found Jackson 3 ObjectMapper");
        } catch (ClassNotFoundException e) {
            try {
                mapperClass = Class.forName("com.fasterxml.jackson.core.ObjectCodec");
                log.debug("Found Jackson 2 ObjectMapper");
            } catch (ClassNotFoundException e1) {
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
