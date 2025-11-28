package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.OnErrorConsumer;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.Objects;

/// Inbound channel handler for processing ERROR frames.
///
/// This handler intercepts ERROR frames.
/// It then passes the error message and the frame to a configured [OnErrorConsumer] for further processing.
/// The consumer is usually set by the user in the [org.schlunzis.zis.stomp.client.StompClientBuilder].
///
/// @see org.schlunzis.zis.stomp.client.StompClientBuilder
/// @see org.schlunzis.zis.stomp.client.StompClientFactory
public final class InboundErrorChannelHandler extends AbstractInboundChannelHandler {

    private final OnErrorConsumer onErrorConsumer;

    public InboundErrorChannelHandler(OnErrorConsumer onErrorConsumer) {
        Objects.requireNonNull(onErrorConsumer, "onErrorConsumer must not be null");
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
