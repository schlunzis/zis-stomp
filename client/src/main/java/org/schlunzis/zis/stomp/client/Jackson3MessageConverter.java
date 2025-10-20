package org.schlunzis.zis.stomp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

public class Jackson3MessageConverter implements MessageConverter {

    private static final Logger log = LoggerFactory.getLogger(Jackson3MessageConverter.class);
    private final ObjectMapper mapper;

    public Jackson3MessageConverter() {
        this(new ObjectMapper());
    }

    public Jackson3MessageConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T convertToType(String messageStr, Class<T> targetType) {
        String trimmed = messageStr.trim();
        log.debug("Converting message to type {}: {}", targetType.getName(), trimmed);
        try {
            return mapper.readValue(trimmed, targetType);
        } catch (RuntimeException e) {
            throw new ConversionException("Error converting string to type " + targetType.getName(), e);
        }
    }

    @Override
    public String convertToString(Object object) {
        return mapper.writeValueAsString(object);
    }

    @Override
    public String contentType() {
        return "application/json;charset=UTF-8";
    }

}
