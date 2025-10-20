package org.schlunzis.zis.stomp.client.subscriptions;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SubscriberSubscriptionFactory {

    private static final Logger log = LoggerFactory.getLogger(SubscriberSubscriptionFactory.class);

    private final MessageConverter messageConverter;

    public SubscriberSubscriptionFactory(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public List<Subscription> createAll(List<Object> subscribers, SubscriptionManager subscriptionManager) {
        return subscribers.stream()
                .flatMap(subscriber -> create(subscriber, subscriptionManager))
                .toList();
    }

    private Stream<Subscription> create(Object subscriber, SubscriptionManager subscriptionManager) {
        final String destinationPrefix = getStompSubscriberAnnotation(subscriber).destinationPrefix();
        final Method[] methods = subscriber.getClass().getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(Topic.class))
                .map(method -> {
                    Topic annotation = method.getAnnotation(Topic.class);
                    final String fullDestination = destinationPrefix + annotation.value();
                    final Class<?> payloadType = getPayloadType(method);

                    return subscriptionManager.create(fullDestination, new MethodInvoker(
                            subscriber,
                            method,
                            payloadType,
                            messageConverter
                    ));
                });
    }

    private StompSubscriber getStompSubscriberAnnotation(Object subscriber) {
        Class<?> clazz = subscriber.getClass();
        StompSubscriber annotation = clazz.getAnnotation(StompSubscriber.class);
        if (annotation == null) {
            annotation = new StompSubscriber() {
                @Override
                public String destinationPrefix() {
                    return "";
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return StompSubscriber.class;
                }
            };
        }
        return annotation;
    }

    private Class<?> getPayloadType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + method.getName() + " must have exactly one parameter");
        }
        return parameterTypes[0];
    }

    private record MethodInvoker(
            Object target,
            Method method,
            Class<?> payloadType,
            MessageConverter messageConverter
    ) implements Consumer<String> {

        @Override
        public void accept(String arg) {
            log.debug("calling subscriber method: {}.{} with arg: {}",
                    target.getClass().getSimpleName(),
                    method.getName(),
                    arg);
            try {
                Object convertedArg = payloadType == String.class
                        ? arg
                        : messageConverter.convertToType(arg, payloadType);
                method.invoke(target, convertedArg);
            } catch (Exception e) {
                log.error("Could not invoke subscriber method: {}", e.getMessage(), e);
            }
        }
    }

}
