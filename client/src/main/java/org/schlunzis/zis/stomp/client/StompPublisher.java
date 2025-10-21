package org.schlunzis.zis.stomp.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a STOMP publisher.
 * <p>
 * Instances of classes annotated with {@code @StompPublisher} can contain methods annotated with {@link Topic}
 * to send messages to specific STOMP destinations. The annotation processor will generate a class
 * that implements the publisher interface, providing methods to send messages to the defined topics.
 * The class has the same name as the annotated interface, with the suffix "Impl" added.
 * It can be instantiated using a constructor that takes a {@link StompClient} as a parameter.
 * <p>
 * The {@code destinationPrefix} element allows
 * specifying a common prefix for all destinations used by the publisher. This prefix will be prepended
 * to the destination values defined in the {@code @Topic} annotations on the publisher's methods.
 * <p>
 * The {@code packageName} and {@code typeName} elements can be used to customize the generated
 * publisher class's package and type names when code generation is involved.
 *
 * @see Topic
 * @see StompSubscriber
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface StompPublisher {

    /**
     * The common destination prefix for all topics used by this publisher.
     * This prefix will be prepended to the destination values defined in the {@link Topic} annotations
     * on the publisher's methods.
     * <p>
     * Default is an empty string, meaning no prefix is applied.
     *
     * @return the destination prefix string
     * @since 1.0.0
     */
    String destinationPrefix() default "";

    /**
     * The package name for the generated publisher class.
     * If not specified, the package of the annotated interface is used.
     *
     * @return the package name
     * @since 1.0.0
     */
    String packageName() default "";

    /**
     * The type name for the generated publisher class.
     * If not specified, the name of the annotated interface with "Impl" suffix is used.
     *
     * @return the type name
     * @since 1.0.0
     */
    String typeName() default "";

}
