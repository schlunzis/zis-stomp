package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/// Inbound channel for handling incoming STOMP frames.
///
/// Frames are received by the WebSocket client and passed to this channel for processing.
/// The channel uses a chain of handlers to process the frames in sequence.
///
/// @see org.schlunzis.zis.stomp.client.websocket.WebSocketClient
/// @see org.schlunzis.zis.stomp.client.internal.Stomp1dot2Client
public class InboundChannel {

    private final TransferQueue<Frame> connectedFrames = new LinkedTransferQueue<>();

    @Nullable
    private AbstractInboundChannelHandler firstHandler;

    /// Sets the first handler in the chain of inbound channel handlers.
    ///
    /// @param firstHandler the first handler to set
    /// @see org.schlunzis.zis.stomp.client.StompClientFactory
    public void setFirstHandler(AbstractInboundChannelHandler firstHandler) {
        this.firstHandler = firstHandler;
    }

    /// Handles an incoming STOMP frame by passing it to the first handler in the chain.
    ///
    /// @param frame the incoming STOMP frame to handle
    public void handle(Frame frame) {
        if (firstHandler != null) {
            firstHandler.handle(frame);
        }
    }

    /// Closes the inbound channel and releases any resources held by the handlers.
    /// Clears the queue of connected frames.
    public void close() {
        connectedFrames.clear();
        if (firstHandler != null) {
            firstHandler.close();
        }
    }

    /// Notifies the inbound channel that a connected frame has been received.
    ///
    /// It is possible to wait for connected frames using the `waitForConnectedFrame` method.
    ///
    /// @param frame the connected STOMP frame
    /// @see InboundConnectedChannelHandler
    public void connected(Frame frame) {
        connectedFrames.add(frame);
    }

    /// Waits for and retrieves the next connected STOMP frame.
    ///
    /// This method returns immediately if a connected frame is already available.
    /// This method blocks until a connected frame is available.
    ///
    /// @return the next connected STOMP frame
    /// @throws InterruptedException if interrupted while waiting
    public Frame waitForConnectedFrame() throws InterruptedException {
        return connectedFrames.take();
    }

}
