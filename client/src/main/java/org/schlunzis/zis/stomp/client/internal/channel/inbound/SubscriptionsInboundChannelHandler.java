package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.internal.SubscriptionManager;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionsInboundChannelHandler extends AbstractInboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsInboundChannelHandler.class);
    private final SubscriptionManager subscriptionManager;

    public SubscriptionsInboundChannelHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handle(Frame frame) {
        log.trace("Handling frame for MESSAGE subscriptions {}", frame);
        if (Command.MESSAGE.equals(frame.command())) {
            subscriptionManager.handleMessage(frame);
        }

        super.handle(frame);
    }

}
