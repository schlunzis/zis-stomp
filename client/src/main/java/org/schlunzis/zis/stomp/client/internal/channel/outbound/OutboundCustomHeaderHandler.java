package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.common.protocol.FrameBuilder;

import java.util.List;
import java.util.Map;

/// Outbound channel handler for adding custom headers to outgoing STOMP frames.
///
/// This handler adds custom headers from the interaction context to the outgoing STOMP frame.
public final class OutboundCustomHeaderHandler extends AbstractOutboundChannelHandler {

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        Map<String, List<String>> headers = context.headers();
        headers.forEach((key, values) ->
                values.reversed().forEach(value ->
                        frameBuilder.header(key, value)
                )
        );

        super.handle(frameBuilder, context);
    }

}
