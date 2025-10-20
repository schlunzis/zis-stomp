package org.schlunzis.zis.stomp.processor.internal;

import java.util.List;

public record Publisher(
        String packageName,
        List<String> imports,
        String name,
        String fullyQualifiedSuperType,
        List<Subscriber> subscribers
) {
}
