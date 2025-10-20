package org.schlunzis.zis.stomp.client.subscriptions;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Subscription;

import java.util.UUID;
import java.util.function.Consumer;

public record StompSubscription(
        SubscriptionManager subscriptionManager,
        UUID id,
        String destination,
        Consumer<String> consumer,
        @Nullable Class<?> payloadType
) implements Subscription {

}
