package org.schlunzis.zis.stomp.mock_server.it.common;

import java.util.UUID;

public record Model(
        UUID id,
        String message
) {
}
