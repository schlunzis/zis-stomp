package org.schlunzis.zis.stomp.client;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * A MessageConverter implementation that uses Jackson 3 to convert messages
 * between JSON string representation and target object types.
 * <p>
 * The content type used by this converter is "application/json;charset=UTF-8".
 *
 * @see Jackson2MessageConverter
 * @since 1.0.0
 */
public class Jackson3MessageConverter implements MessageConverter {

    private final ObjectMapper mapper;

    /**
     * Creates a new MessageConverter using a default Jackson 3 ObjectMapper.
     *
     * @since 1.0.0
     */
    public Jackson3MessageConverter() {
        this(new ObjectMapper());
    }

    /**
     * Creates a new MessageConverter using the provided Jackson 3 ObjectMapper.
     *
     * @param mapper the ObjectMapper to use for JSON serialization and deserialization
     * @since 1.0.0
     */
    public Jackson3MessageConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T convertToType(String messageStr, Class<T> targetType) {
        try {
            return mapper.readValue(messageStr, targetType);
        } catch (RuntimeException e) {
            throw new ConversionException("Error converting string to type " + targetType.getName(), e);
        }
    }

    @Override
    public String convertToString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ConversionException("Error converting object to string", e);
        }
    }

    @Override
    public String contentType() {
        return "application/json;charset=UTF-8";
    }

}
