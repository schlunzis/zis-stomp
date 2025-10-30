package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.Subscription;

import java.util.UUID;

public record StompSubscription(
        SubscriptionManager subscriptionManager,
        UUID id,
        String destination,
        SubscriberInvoker invoker
) implements Subscription {

}
