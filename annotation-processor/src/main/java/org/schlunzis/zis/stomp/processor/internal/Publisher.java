package org.schlunzis.zis.stomp.processor.internal;

import java.util.List;

/// Record representing a Publisher with its metadata and associated Subscribers.
///
/// @param packageName             the package to generate the publisher in
/// @param imports                 the imports required for the publisher
/// @param name                    the name of the publisher class
/// @param fullyQualifiedSuperType the fully qualified name of the super type/interface
/// @param subscribers             the list of subscribers associated with the publisher
/// @since 1.0.0
public record Publisher(
        String packageName,
        List<String> imports,
        String name,
        String fullyQualifiedSuperType,
        List<Subscriber> subscribers
) {
}
