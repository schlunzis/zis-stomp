package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

/// Abstract outbound channel handler for processing outgoing STOMP frames.
///
/// The handlers are called by the [OutboundChannel] to process outgoing STOMP frames.
/// Implementation classes should override the [#handle(FrameBuilder, InteractionContext)] method to provide specific
/// processing logic.
/// They must then call the superclass method to pass the frame to the next handler in the chain.
/// They could also override the [#close()] method to release any resources held by the handler.
/// They must call the superclass method to ensure that the next handler in the chain is also closed.
///
/// @see OutboundChannel
public abstract class AbstractOutboundChannelHandler {

    @Nullable
    private AbstractOutboundChannelHandler next;

    /// Sets the next handler in the chain.
    ///
    /// @param next the next handler to set
    public void setNext(AbstractOutboundChannelHandler next) {
        this.next = next;
    }

    /// Handles an outgoing STOMP frame.
    ///
    /// Implementation classes should override this method to provide specific processing logic.
    /// They must call the superclass method to pass the frame to the next handler in the chain.
    ///
    /// @param frameBuilder the outgoing STOMP frame builder modify the frame to send
    /// @param context      the interaction context with more information about the interaction
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        if (next != null) {
            next.handle(frameBuilder, context);
        }
    }

    /// Closes the handler and releases any resources held by it.
    /// Implementation classes should override this method to release their resources.
    /// They must call the superclass method to ensure that the next handler in the chain is also closed.
    public void close() {
        if (next != null) {
            next.close();
        }
    }

}
