package org.schlunzis.zis.stomp.processor;

import java.util.List;

public record Publisher(
        String packageName,
        List<String> imports,
        String name,
        String fullyQualifiedSuperType,
        List<Subscriber> subscribers
) {
}
