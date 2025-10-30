package org.schlunzis.zis.stomp.client;

import java.util.Objects;

/// A MessageConverter implementation that returns the message as-is for String types.
/// It is expected that the message is already a string.
/// Otherwise, an IllegalArgumentException is thrown.
///
/// This converter should not be used for more complex applications.
/// Use [Jackson2MessageConverter], [Jackson3MessageConverter] or provide your own
/// implementation of [MessageConverter] instead.
///
/// This message converter always uses the content type "text/plain;charset=UTF-8".
///
/// @see Jackson3MessageConverter
/// @see Jackson2MessageConverter
/// @since 1.0.0
public class StringMessageConverter implements MessageConverter {

    /// Creates a new StringMessageConverter.
    ///
    /// @since 1.0.0
    public StringMessageConverter() {
        // No special initialization required
    }

    @Override
    public <T> T convertToType(String messageStr, Class<T> targetType) {
        if (!String.class.equals(targetType)) {
            throw new IllegalArgumentException("StringMessageConverter can only convert to String type. Consider setting a different MessageConverter during StompClient construction.");
        }
        //noinspection unchecked
        return (T) messageStr;
    }

    @Override
    public String convertToString(Object object) {
        return Objects.toString(object);
    }

    @Override
    public String contentType() {
        return "text/plain;charset=UTF-8";
    }

}
