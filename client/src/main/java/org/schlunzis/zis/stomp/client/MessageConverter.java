package org.schlunzis.zis.stomp.client;

public interface MessageConverter {

    <T> T convertToType(String messageStr, Class<T> targetType);

    String convertToString(Object object);

    String contentType();

}
