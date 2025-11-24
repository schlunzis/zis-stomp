package org.schlunzis.zis.stomp.client.internal;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.StompSubscriber;
import org.schlunzis.zis.stomp.client.Topic;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/// This class is responsible for extracting subscription information from objects
/// annotated with [StompSubscriber] and [Topic] annotations.
///
/// It creates [StompSubscription] instances for each annotated method in the subscriber object.
///
/// @see SubscriberInvoker
/// @see SubscriptionManager
/// @see StompSubscriber
/// @see Topic
final class AnnotatedSubscriberHandler {

    private final MessageConverter messageConverter;

    @Nullable
    private SubscriptionManager subscriptionManager;

    AnnotatedSubscriberHandler(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    void subscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    List<StompSubscription> handle(Object subscriber) {
        return create(subscriber).toList();
    }

    /// Creates all subscriptions for the given subscriber object.
    ///
    /// @param subscriber The subscriber object containing annotated methods.
    /// @return A stream of [StompSubscription] instances created from the annotated methods.
    private Stream<StompSubscription> create(Object subscriber) {
        final SubscriberConfiguration config = getSubscriberConfiguration(subscriber);
        final String destinationPrefix = config.destinationPrefix();
        final Method[] methods = subscriber.getClass().getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(Topic.class))
                .map(method -> {
                    Topic annotation = method.getAnnotation(Topic.class);
                    final String fullDestination = destinationPrefix + annotation.value();
                    final Class<?> payloadType = getPayloadType(method);

                    return subscriptionManager.create(fullDestination, new SubscriberInvoker<>(
                            messageConverter,
                            payloadType,
                            method,
                            subscriber
                    ));
                });
    }

    /// Retrieves the subscriber configuration from the [StompSubscriber] annotation.
    /// If the annotation is not present, default configuration values are used.
    ///
    /// @param subscriber The subscriber object.
    /// @return The [SubscriberConfiguration] extracted from the annotation or default values.
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

    /// Retrieves the payload type from the method's parameter.
    ///
    /// The method must have exactly one parameter.
    ///
    /// @param method The method to inspect.
    /// @return The class of the method's single parameter.
    private Class<?> getPayloadType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method " + method.getName() + " must have exactly one parameter");
        }
        return parameterTypes[0];
    }

    /// Configuration data for a subscriber extracted from the [StompSubscriber] annotation.
    ///
    /// @param destinationPrefix The prefix to be added to all subscription destinations.
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
