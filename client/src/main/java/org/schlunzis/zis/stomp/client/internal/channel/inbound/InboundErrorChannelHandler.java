package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.OnErrorConsumer;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

public class InboundErrorChannelHandler extends AbstractInboundChannelHandler {

    private final OnErrorConsumer onErrorConsumer;

    public InboundErrorChannelHandler(OnErrorConsumer onErrorConsumer) {
        this.onErrorConsumer = onErrorConsumer;
    }

    @Override
    public void handle(Frame frame) {
        if (Command.ERROR.equals(frame.command())) {
            String message = frame.headers().getFirst("message");
            if (message == null) {
                message = "Unknown error";
            }
            onErrorConsumer.accept(message, frame);
        }

        super.handle(frame);
    }

}
