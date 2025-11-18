package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Frame;

public abstract class AbstractInboundChannelHandler {

    @Nullable
    private AbstractInboundChannelHandler next;

    public void setNext(AbstractInboundChannelHandler next) {
        this.next = next;
    }

    public void handle(Frame frame) {
        if (next != null) {
            next.handle(frame);
        }
    }

    public void close() {
        if (next != null) {
            next.close();
        }
    }

}
