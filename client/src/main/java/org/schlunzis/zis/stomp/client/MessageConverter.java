package org.schlunzis.zis.stomp.client;

/**
 * Converts messages between String representation and target object types.
 *
 * @since 1.0.0
 */
public interface MessageConverter {

    /**
     * Converts the given message string to the specified target type.
     *
     * @param messageStr the message in String format
     * @param targetType the desired target type class
     * @param <T>        the target type
     * @return the converted message object of the specified target type
     * @throws ConversionException if the conversion fails
     * @since 1.0.0
     */
    <T> T convertToType(String messageStr, Class<T> targetType) throws ConversionException;

    /**
     * Converts the given object to its String representation.
     * <p>
     * The string must be UTF-8 encoded.
     *
     * @param object the object to convert
     * @return the String representation of the object
     * @since 1.0.0
     */
    String convertToString(Object object);

    /**
     * Returns the content type associated with this message converter.
     * This content type is used in STOMP message headers to indicate
     * the format of the message body.
     * <p>
     * This is typically a MIME type, such as "text/plain;charset=UTF-8" or "application/json;charset=UTF-8".
     *
     * @return the content type as a String
     * @since 1.0.0
     */
    String contentType();

}
