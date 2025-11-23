package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.Subscription;

import java.util.UUID;

/// Internal representation of a STOMP subscription.
///
/// @param subscriptionManager The manager responsible for handling the subscription.
/// @param id                  The unique identifier of the subscription.
/// @param destination         The destination to which the subscription is made.
/// @param invoker             The invoker responsible for calling the subscriber's message handler.
public record StompSubscription(
        SubscriptionManager subscriptionManager,
        UUID id,
        String destination,
        SubscriberInvoker invoker
) implements Subscription {
}
