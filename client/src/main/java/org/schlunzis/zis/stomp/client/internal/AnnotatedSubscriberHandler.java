package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.Topic;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

final class AnnotatedSubscriberHandler {

    private final SubscriptionManager subscriptionManager;
    private final MessageConverter messageConverter;

    AnnotatedSubscriberHandler(SubscriptionManager subscriptionManager, MessageConverter messageConverter) {
        this.subscriptionManager = subscriptionManager;
        this.messageConverter = messageConverter;
    }

    List<Subscription> handle(Object subscriber) {
        return create(subscriber).toList();
    }

    private Stream<Subscription> create(Object subscriber) {
        final SubscriberConfiguration config = getSubscriberConfiguration(subscriber);
        final String destinationPrefix = config.destinationPrefix();
        final Method[] methods = subscriber.getClass().getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(Topic.class))
                .map(method -> {
                    Topic annotation = method.getAnnotation(Topic.class);
                    final String fullDestination = destinationPrefix + annotation.value();
                    final Class<?> payloadType = getPayloadType(method);

                    return subscriptionManager.create(fullDestination, new SubscriberInvoker(
                            messageConverter,
                            payloadType,
                            method,
                            subscriber
                    ));
                });
    }

    private SubscriberConfiguration getSubscriberConfiguration(Object subscriber) {
        Class<?> clazz = subscriber.getClass();
        StompSubscriber annotation = clazz.getAnnotation(StompSubscriber.class);
        if (annotation == null) {
            return SubscriberConfiguration.getDefault();
        }
        return new SubscriberConfiguration(
                annotation.destinationPrefix()
        );
    }

    private Class<?> getPayloadType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + method.getName() + " must have exactly one parameter");
        }
        return parameterTypes[0];
    }

    private record SubscriberConfiguration(
            String destinationPrefix
    ) {
        static SubscriberConfiguration getDefault() {
            return new SubscriberConfiguration(
                    ""
            );
        }
    }

}
