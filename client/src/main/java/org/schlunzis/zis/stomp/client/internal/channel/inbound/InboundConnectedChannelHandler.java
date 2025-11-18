package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.Objects;
import java.util.function.Consumer;

public class InboundConnectedChannelHandler extends AbstractInboundChannelHandler {

    @Nullable
    private Consumer<Frame> connectedFrameConsumer;

    @Override
    public void handle(Frame frame) {
        if (Command.CONNECTED.equals(frame.command()) && connectedFrameConsumer != null) {
            connectedFrameConsumer.accept(frame);
        }

        super.handle(frame);
    }

    public void setConnectedFrameConsumer(Consumer<Frame> connectedFrameConsumer) {
        Objects.requireNonNull(connectedFrameConsumer, "connectedFrameConsumer must not be null");
        this.connectedFrameConsumer = connectedFrameConsumer;
    }

}
