package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.connection.StompSession;

public record Subscription<SESSION extends StompSession>(
        SESSION session,
        String id
) {
}
