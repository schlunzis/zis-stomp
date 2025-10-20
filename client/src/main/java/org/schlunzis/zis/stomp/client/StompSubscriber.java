package org.schlunzis.zis.stomp.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Annotation to mark a class as a STOMP subscriber.
 * <p>
 * Instances of classes annotated with {@code @StompSubscriber} can contain methods annotated with {@link Topic}
 * to handle messages from specific STOMP destinations. The {@code destinationPrefix} element allows
 * specifying a common prefix for all destinations handled by the subscriber. This prefix will be prepended
 * to the destination values defined in the {@code @Topic} annotations on the subscriber's methods.
 * <p>
 * To register a subscriber with a {@link StompClient}, pass an instance of the subscriber class
 * to the client builder using the {@link StompClientBuilder#subscribers(List)} method.
 *
 * @see Topic
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StompSubscriber {

    String destinationPrefix() default "";

}
