package org.schlunzis.zis.stomp.processor.internal;

/// Record representing a Subscriber with its metadata.
///
/// @param topic                       the topic the subscriber is subscribed to
/// @param methodName                  the method name to handle messages for the topic
/// @param fullyQualifiedParameterType the fully qualified type of the method parameter
/// @param parameterName               the name of the method parameter
/// @param returnType                  the return type of the method
/// @param async                       whether the method returns a [java.util.concurrent.CompletableFuture] or not
public record Subscriber(
        String topic,
        String methodName,
        String fullyQualifiedParameterType,
        String parameterName,
        String returnType,
        boolean async
) {
}
