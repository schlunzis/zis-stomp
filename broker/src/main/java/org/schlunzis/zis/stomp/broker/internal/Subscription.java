package org.schlunzis.zis.stomp.broker.internal;

public record Subscription<SESSION>(
        SESSION session,
        String id
) {
}
