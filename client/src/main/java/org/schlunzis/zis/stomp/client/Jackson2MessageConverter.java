package org.schlunzis.zis.stomp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A MessageConverter implementation that uses Jackson 2 to convert messages
 * between JSON string representation and target object types.
 * <p>
 * The content type used by this converter is "application/json;charset=UTF-8".
 *
 * @see Jackson3MessageConverter
 * @since 1.0.0
 */
public class Jackson2MessageConverter implements MessageConverter {

    private final ObjectMapper mapper;

    /**
     * Creates a new MessageConverter using a default Jackson 2 ObjectMapper.
     *
     * @since 1.0.0
     */
    public Jackson2MessageConverter() {
        this(new ObjectMapper());
    }

    /**
     * Creates a new MessageConverter using the provided Jackson 2 ObjectMapper.
     *
     * @param mapper the ObjectMapper to use for JSON serialization and deserialization
     * @since 1.0.0
     */
    public Jackson2MessageConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T convertToType(String messageStr, Class<T> targetType) {
        try {
            return mapper.readValue(messageStr, targetType);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error converting string to type " + targetType.getName(), e);
        }
    }

    @Override
    public String convertToString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error converting object to string", e);
        }
    }

    @Override
    public String contentType() {
        return "application/json;charset=UTF-8";
    }

}
