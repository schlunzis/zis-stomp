package org.schlunzis.zis.stomp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson2MessageConverter implements MessageConverter {

    private final ObjectMapper mapper;

    public Jackson2MessageConverter() {
        this(new ObjectMapper());
    }

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
