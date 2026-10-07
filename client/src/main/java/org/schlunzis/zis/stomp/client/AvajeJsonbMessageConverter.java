package org.schlunzis.zis.stomp.client;

import io.avaje.json.JsonException;
import io.avaje.jsonb.Jsonb;

/// A MessageConverter implementation that uses Jsonb to convert messages
/// between JSON string representation and target object types.
///
/// The content type used by this converter is `application/json;charset=UTF-8`.
///
/// @since 1.0.0
public class AvajeJsonbMessageConverter implements MessageConverter {

    private final Jsonb jsonb;

    /// Creates a new MessageConverter using a default Jsonb instance.
    ///
    /// @since 1.0.0
    public AvajeJsonbMessageConverter() {
        this(Jsonb.builder().build());
    }

    /// Creates a new MessageConverter using the provided Jsonb instance.
    ///
    /// @param jsonb the Jsonb instance to use for JSON serialization and deserialization
    /// @since 1.0.0
    public AvajeJsonbMessageConverter(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public <T> T convertToType(String messageStr, Class<T> targetType) {
        try {
            return jsonb.type(targetType).fromJson(messageStr);
        } catch (RuntimeException e) {
            throw new ConversionException("Error converting string to type " + targetType.getName(), e);
        }
    }

    @Override
    public String convertToString(Object object) {
        try {
            return jsonb.toJson(object);
        } catch (JsonException e) {
            throw new ConversionException("Error converting object to string", e);
        }
    }

    @Override
    public String contentType() {
        return "application/json;charset=UTF-8";
    }

}
