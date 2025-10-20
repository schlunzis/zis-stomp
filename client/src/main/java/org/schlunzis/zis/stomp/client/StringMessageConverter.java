package org.schlunzis.zis.stomp.client;

import java.util.Objects;

public class StringMessageConverter implements MessageConverter {

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
