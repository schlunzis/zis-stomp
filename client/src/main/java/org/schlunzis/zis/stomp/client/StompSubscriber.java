package org.schlunzis.zis.stomp.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a STOMP subscriber.
 * <p>
 * Instances of classes annotated with {@code @StompSubscriber} can contain methods annotated with {@link Topic}
 * to handle messages from specific STOMP destinations. The {@code destinationPrefix} element allows
 * specifying a common prefix for all destinations handled by the subscriber. This prefix will be prepended
 * to the destination values defined in the {@code @Topic} annotations on the subscriber's methods.
 * <p>
 * To register a subscriber with a {@link StompClient}, pass an instance of the subscriber method
 * to the client's {@link StompClient#subscribe(Object)} method.
 * To unregister the subscriber and unsubscribe from all its topics, pass the same instance
 * to the client's {@link StompClient#unsubscribe(Object)} method.
 * <p>
 * The hash code of the subscriber instance may change. This has no effect on subscription management,
 * as the client uses identity comparison to track subscribers.
 * <p>
 * Methods annotated with {@link Topic} in a subscriber class must be instance methods
 * (non-static) and have a single parameter representing the message payload type.
 * The message payload will be deserialized to the parameter type before invoking the method.
 * The return type of the method is ignored. It is recommended to use {@code void} return type.
 *
 * @see Topic
 * @see StompPublisher
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StompSubscriber {

    /**
     * The common destination prefix for all topics handled by this subscriber.
     * This prefix will be prepended to the destination values defined in the {@link Topic} annotations
     * on the subscriber's methods.
     * <p>
     * Default is an empty string, meaning no prefix is applied.
     *
     * @return the destination prefix string
     * @since 1.0.0
     */
    String destinationPrefix() default "";

}
