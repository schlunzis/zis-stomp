package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.common.protocol.FrameBuilder;

/// Outbound channel for processing outgoing STOMP frames.
///
/// This channel gets [FrameBuilder]s and [InteractionContext]s to process outgoing STOMP frames.
/// It delegates the processing to the first handler.
///
/// After invoking the first handler, it waits for the interaction context to complete
/// before returning control to the caller.
///
/// @see AbstractOutboundChannelHandler
/// @see org.schlunzis.zis.stomp.client.StompClientFactory
public class OutboundChannel {

    @Nullable
    private AbstractOutboundChannelHandler firstHandler;

    /// Sets the first handler in the chain of outbound channel handlers.
    ///
    /// @param firstHandler the first handler to set
    /// @see org.schlunzis.zis.stomp.client.StompClientFactory
    public void setFirstHandler(AbstractOutboundChannelHandler firstHandler) {
        this.firstHandler = firstHandler;
    }

    /// Handles an outgoing STOMP frame by passing it to the first handler in the chain.
    ///
    /// @param frameBuilder the outgoing STOMP frame builder modify the frame to send
    /// @param context      the interaction context with more information about the interaction
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        if (firstHandler != null) {
            firstHandler.handle(frameBuilder, context);
            context.awaitCompletion();
        }
    }

    /// Closes the outbound channel and releases any resources held by the handlers.
    /// Invokes the close method on the first handler in the chain.
    public void close() {
        if (firstHandler != null) {
            firstHandler.close();
        }
    }

}
