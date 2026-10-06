package org.schlunzis.zis.stomp.common;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/// Represents the headers of a STOMP frame.
///
/// Headers are key-value pairs that provide metadata about the STOMP frame.
/// Each key can have multiple values.
/// The first value for a given key can be retrieved using the [#getFirst(String)] method.
/// The first value is also the true value. Subsequent values are provided to give a history of changes to that value
/// over eventual multiple hops.
///
/// Implementations may not be thread-safe.
///
/// @since 1.0.0
public interface Headers extends Map<String, List<String>> {

    /// Protocol related header
    String ACCEPT_VERSION = "accept-version";
    /// Protocol related header
    String ACK = "ack";
    /// Protocol related header
    String CONTENT_LENGTH = "content-length";
    /// Protocol related header
    String CONTENT_TYPE = "content-type";
    /// Protocol related header
    String DESTINATION = "destination";
    /// Protocol related header
    String HEART_BEAT = "heart-beat";
    /// Protocol related header
    String HOST = "host";
    /// Protocol related header
    String ID = "id";
    /// Protocol related header
    String LOGIN = "login";
    /// Protocol related header
    String MESSAGE_ID = "message-id";
    /// Protocol related header
    String PASSCODE = "passcode";
    /// Protocol related header
    String RECEIPT = "receipt";
    /// Protocol related header
    String RECEIPT_ID = "receipt-id";
    /// Protocol related header
    String SERVER = "server";
    /// Protocol related header
    String SESSION = "session";
    /// Protocol related header
    String SUBSCRIPTION = "subscription";
    /// Protocol related header
    String TRANSACTION = "transaction";
    /// Protocol related header
    String VERSION = "version";

    /// Adds a header with the specified key and value.
    /// If the key already exists, the new value is added as the first value for that key
    /// Meaning it takes precedence over existing values.
    ///
    /// @param key   the header key
    /// @param value the header value
    /// @since 1.0.0
    void addFirst(String key, String value);

    /// Returns the first value associated with the specified key.
    /// If the key does not exist, returns null.
    ///
    /// @param key the header key
    /// @return the first header value, or null if the key does not exist
    /// @since 1.0.0
    @Nullable String getFirst(String key);

}
