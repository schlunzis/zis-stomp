package org.schlunzis.zis.stomp.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface StompPublisher {

    String destinationPrefix() default "";

    String packageName() default "";

    String typeName() default "";

}
