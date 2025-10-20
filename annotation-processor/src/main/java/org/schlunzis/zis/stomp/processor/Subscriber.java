package org.schlunzis.zis.stomp.processor;

public record Subscriber(
        String topic,
        String methodName,
        String fullyQualifiedParameterType,
        String parameterName
) {
}
