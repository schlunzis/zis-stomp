package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

final class SubscriberInvoker {

    private static final Logger log = LoggerFactory.getLogger(SubscriberInvoker.class);

    private final MessageConverter messageConverter;

    private final Class<?> payloadType;
    private final Method method;
    private final Object target;

    SubscriberInvoker(MessageConverter messageConverter, Class<?> payloadType, Method method, Object target) {
        this.messageConverter = messageConverter;
        this.payloadType = payloadType;
        this.method = method;
        this.target = target;
    }

    SubscriberInvoker(MessageConverter messageConverter, Class<?> payloadType, Consumer<?> consumer) {
        this.messageConverter = messageConverter;
        this.payloadType = payloadType;
        try {
            this.method = Consumer.class.getMethod("accept", Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find accept method on Consumer", e);
        }
        this.target = consumer;
    }

    void invoke(Frame frame) {
        Optional<String> body = frame.body();
        Object payload;
        if (payloadType.equals(String.class)) {
            payload = body.orElse("");
        } else {
            payload = messageConverter.convertToType(body.orElse(""), payloadType);
        }

        try {
            method.invoke(target, payload);
        } catch (Exception e) {
            log.error("Could not invoke subscriber method: {}", e.getMessage(), e);
        }
    }

}
