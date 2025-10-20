package org.schlunzis.zis.stomp.processor.internal;

public record Subscriber(
        String topic,
        String methodName,
        String fullyQualifiedParameterType,
        String parameterName
) {
}
