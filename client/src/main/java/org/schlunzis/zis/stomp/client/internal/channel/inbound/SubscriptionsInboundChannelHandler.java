package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.schlunzis.zis.stomp.client.internal.SubscriptionManager;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

public class SubscriptionsInboundChannelHandler extends AbstractInboundChannelHandler {

    private final SubscriptionManager subscriptionManager;

    public SubscriptionsInboundChannelHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handle(Frame frame) {
        if (Command.MESSAGE.equals(frame.command())) {
            subscriptionManager.handleMessage(frame);
        }

        super.handle(frame);
    }

}
