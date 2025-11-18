package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

public abstract class AbstractOutboundChannelHandler {

    @Nullable
    private AbstractOutboundChannelHandler next;

    public void setNext(AbstractOutboundChannelHandler next) {
        this.next = next;
    }

    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        if (next != null) {
            next.handle(frameBuilder, context);
        }
    }

    public void close() {
        if (next != null) {
            next.close();
        }
    }

}
