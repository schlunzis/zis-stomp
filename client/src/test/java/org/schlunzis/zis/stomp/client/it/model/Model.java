package org.schlunzis.zis.stomp.client.it.model;

import java.util.UUID;

public record Model(
        UUID id,
        String message
) {
}
