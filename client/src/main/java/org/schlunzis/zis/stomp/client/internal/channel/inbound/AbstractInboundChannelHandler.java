package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Frame;

/// Abstract base class for inbound channel handlers.
///
/// The handlers are called by the [InboundChannel] to process incoming STOMP frames.
/// Implementation classes should override the [#handle(Frame)] method to provide specific processing logic.
/// They must then call the superclass method to pass the frame to the next handler in the chain.
/// They should also override the [#close()] method to release any resources held by the handler.
/// They must call the superclass method to ensure that the next handler in the chain is also closed.
///
/// @see InboundChannel
public abstract class AbstractInboundChannelHandler {

    @Nullable
    private AbstractInboundChannelHandler next;

    /// Sets the next handler in the chain.
    ///
    /// @param next the next handler to set
    public void setNext(AbstractInboundChannelHandler next) {
        this.next = next;
    }

    /// Handles an incoming STOMP frame.
    ///
    /// Implementation classes should override this method to provide specific processing logic.
    /// They must call the superclass method to pass the frame to the next handler in the chain.
    ///
    /// @param frame the incoming STOMP frame to handle
    public void handle(Frame frame) {
        if (next != null) {
            next.handle(frame);
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
