package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

public class OutboundChannel {

    @Nullable
    private AbstractOutboundChannelHandler firstHandler;

    public void setFirstHandler(AbstractOutboundChannelHandler firstHandler) {
        this.firstHandler = firstHandler;
    }

    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        if (firstHandler != null) {
            firstHandler.handle(frameBuilder, context);
            context.awaitCompletion();
        }
    }

    public void close() {
        if (firstHandler != null) {
            firstHandler.close();
        }
    }

}
