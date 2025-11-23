package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.SubscribeContext;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

/// Invokes subscriber methods with converted message payloads.
///
/// This class is responsible for invoking the appropriate subscriber method
/// when a message is received. It converts the message payload to the expected
/// type before invoking the method.
///
/// @see SubscriptionManager
/// @see org.schlunzis.zis.stomp.client.StompClient#subscribe(SubscribeContext)
/// @see org.schlunzis.zis.stomp.client.StompClient#subscribe(Object)
public final class SubscriberInvoker<T> {

    private static final Logger log = LoggerFactory.getLogger(SubscriberInvoker.class);

    private final MessageConverter messageConverter;

    private final Class<?> payloadType;
    private final Method method;
    private final Object target;

    /// Creates a new SubscriberInvoker which is calling the given method on the target object.
    ///
    /// The method to invoke must expect a single parameter of the given payload type.
    ///
    /// @param messageConverter The message converter to use for payload conversion.
    /// @param payloadType      The expected payload type for the subscriber method.
    /// @param method           The method to invoke on message receipt.
    /// @param target           The target object on which to invoke the method.
    public SubscriberInvoker(MessageConverter messageConverter, Class<T> payloadType, Method method, Object target) {
        this.messageConverter = messageConverter;
        this.payloadType = payloadType;
        this.method = method;
        this.target = target;
    }

    /// Creates a new SubscriberInvoker which is calling the given consumer.
    ///
    /// @param messageConverter The message converter to use for payload conversion.
    /// @param payloadType      The expected payload type for the subscriber method.
    /// @param consumer         The consumer to invoke on message receipt.
    public SubscriberInvoker(MessageConverter messageConverter, Class<T> payloadType, Consumer<T> consumer) {
        this.messageConverter = messageConverter;
        this.payloadType = payloadType;
        try {
            this.method = Consumer.class.getMethod("accept", Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find accept method on Consumer", e); // This should never happen
        }
        this.target = consumer;
    }

    /// Invokes the subscriber method with the converted payload from the given frame.
    ///
    /// @param frame The STOMP frame containing the message to process.
    public void invoke(Frame frame) {
        Optional<String> body = frame.body();
        Object payload;
        if (payloadType.equals(String.class)) {
            payload = body.orElse("");
        } else {
            payload = messageConverter.convertToType(body.orElse(""), payloadType);
        }

        try {
            method.invoke(target, payload);
        } catch (IllegalAccessException e) {
            log.error("Could not invoke subscriber method: {}", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error("Exception thrown by subscriber method: {}", e.getTargetException().getMessage(), e.getTargetException());
        }
    }

}
