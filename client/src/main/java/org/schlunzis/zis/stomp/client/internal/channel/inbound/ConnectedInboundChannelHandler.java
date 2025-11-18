package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

public class ConnectedInboundChannelHandler extends AbstractInboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(ConnectedInboundChannelHandler.class);
    @Nullable
    private Consumer<Frame> connectedFrameConsumer;

    @Override
    public void handle(Frame frame) {
        log.trace("Handling frame for CONNECTED {}", frame);
        if (Command.CONNECTED.equals(frame.command()) && connectedFrameConsumer != null) {
            connectedFrameConsumer.accept(frame);
        }

        super.handle(frame);
    }

    public void setConnectedFrameConsumer(Consumer<Frame> connectedFrameConsumer) {
        log.trace("setConnectedFrameConsumer({})", connectedFrameConsumer);

        Objects.requireNonNull(connectedFrameConsumer, "connectedFrameConsumer must not be null");
        this.connectedFrameConsumer = connectedFrameConsumer;
    }

}
