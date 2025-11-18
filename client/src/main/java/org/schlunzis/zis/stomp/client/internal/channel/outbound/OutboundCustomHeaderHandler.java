package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class OutboundCustomHeaderHandler extends AbstractOutboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(OutboundCustomHeaderHandler.class);

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        log.trace("Adding {} custom headers to outbound frame", context.headers().size());
        Map<String, List<String>> headers = context.headers();
        headers.forEach((key, values) ->
                values.reversed().forEach(value ->
                        frameBuilder.header(key, value)
                )
        );

        super.handle(frameBuilder, context);
    }

}
