package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

import java.util.List;
import java.util.Map;

public class OutboundCustomHeaderHandler extends AbstractOutboundChannelHandler {

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
