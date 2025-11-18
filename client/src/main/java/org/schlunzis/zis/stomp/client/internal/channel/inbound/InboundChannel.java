package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class InboundChannel {

    private final TransferQueue<Frame> connectedFrames = new LinkedTransferQueue<>();

    @Nullable
    private AbstractInboundChannelHandler firstHandler;

    public void setFirstHandler(AbstractInboundChannelHandler firstHandler) {
        this.firstHandler = firstHandler;
    }

    public void handle(Frame frame) {
        if (firstHandler != null) {
            firstHandler.handle(frame);
        }
    }

    public void close() {
        connectedFrames.clear();
        if (firstHandler != null) {
            firstHandler.close();
        }
    }

    public void connected(Frame frame) {
        connectedFrames.add(frame);
    }

    public Frame waitForConnectedFrame() throws InterruptedException {
        return connectedFrames.take();
    }

}
