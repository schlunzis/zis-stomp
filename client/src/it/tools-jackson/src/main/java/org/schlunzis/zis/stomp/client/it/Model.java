package org.schlunzis.zis.stomp.client.it;

import java.util.UUID;

public record Model(
        UUID id,
        String message
) {
}
