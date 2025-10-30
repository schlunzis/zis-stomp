package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.HeadersImpl;

import java.util.List;
import java.util.Map;

/**
 * Represents the headers of a STOMP frame.
 * <p>
 * Headers are key-value pairs that provide metadata about the STOMP frame.
 * Each key can have multiple values.
 * The first value for a given key can be retrieved using the {@link #getFirst(String)} method.
 * The first value is also the true value. Subsequent values are provided to give a history of changes to that value
 * over eventual multiple hops.
 * <p>
 * Implementations may not be thread-safe.
 *
 * @since 1.0.0
 */
public sealed interface Headers extends Map<String, List<String>> permits HeadersImpl {

    /**
     * Adds a header with the specified key and value.
     * If the key already exists, the new value is added as the first value for that key
     * Meaning it takes precedence over existing values.
     *
     * @param key   the header key
     * @param value the header value
     * @since 1.0.0
     */
    void addFirst(String key, String value);

    /**
     * Returns the first value associated with the specified key.
     * If the key does not exist, returns null.
     *
     * @param key the header key
     * @return the first header value, or null if the key does not exist
     * @since 1.0.0
     */
    @Nullable String getFirst(String key);

}
