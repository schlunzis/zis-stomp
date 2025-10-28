package org.schlunzis.zis.stomp.client.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Subscription;

import java.util.UUID;

public record StompSubscription(
        SubscriptionManager subscriptionManager,
        UUID id,
        String destination,
        SubscriberInvoker invoker,
        @Nullable Class<?> payloadType
) implements Subscription {

}
