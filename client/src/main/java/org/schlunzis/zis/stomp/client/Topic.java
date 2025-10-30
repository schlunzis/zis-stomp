package org.schlunzis.zis.stomp.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotation to mark methods that handle messages from or to a specific STOMP topic.
///
/// @see StompSubscriber
/// @see StompPublisher
/// @since 1.0.0
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

    /// The STOMP topic to use for subscriptions or sending messages.
    ///
    /// @return the topic string
    /// @since 1.0.0
    String value() default "";

}
