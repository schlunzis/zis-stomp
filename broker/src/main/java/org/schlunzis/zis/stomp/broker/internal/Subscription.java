package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

public record Subscription<SESSION extends WebsocketSession>(
        SESSION session,
        String id
) {
}
