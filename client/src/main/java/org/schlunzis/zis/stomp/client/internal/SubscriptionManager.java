package org.schlunzis.zis.stomp.client.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

    private final Map<UUID, StompSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<Object, Set<Subscription>> subscriberSubscriptions = new ConcurrentHashMap<>();

    private final AnnotatedSubscriberHandler annotatedSubscriberHandler;

    SubscriptionManager(MessageConverter messageConverter) {
        this.annotatedSubscriberHandler = new AnnotatedSubscriberHandler(this, messageConverter);
    }

    Set<Subscription> createAnnotatedSubscriptions(Object subscriber) {
        Set<Subscription> s = new HashSet<>(annotatedSubscriberHandler.handle(subscriber));
        subscriberSubscriptions.put(subscriber, s);
        return s;
    }

    Subscription create(String destination, SubscriberInvoker invoker) {
        StompSubscription subscription = new StompSubscription(
                this,
                UUID.randomUUID(),
                destination,
                invoker,
                null
        );
        subscriptions.put(subscription.id(), subscription);
        return subscription;
    }

    void handleMessage(Frame frame) {
        String subscriptionId = frame.headers().getFirst("subscription");
        if (subscriptionId == null) {
            log.warn("Received frame without subscription id: {}", frame);
            return;
        }

        try {
            UUID id = UUID.fromString(subscriptionId);
            StompSubscription subscription = subscriptions.get(id);
            if (subscription != null) {
                subscription.invoker().invoke(frame);
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast subscription for id {}: {}", subscriptionId, e.getMessage());
        }
    }

    void remove(Subscription stompSubscription) {
        subscriptions.remove(stompSubscription.id());
    }

    boolean contains(UUID id) {
        return subscriptions.containsKey(id);
    }

    boolean hasSubscriptionsForSubscriber(Object subscriber) {
        return subscriberSubscriptions.containsKey(subscriber);
    }

    @Nullable Set<Subscription> remove(Object subscriber) {
        Set<Subscription> subs = subscriberSubscriptions.remove(subscriber);
        if (subs != null) {
            for (Subscription sub : subs) {
                subscriptions.remove(sub.id());
            }
        }
        return subs;
    }

    public void clear() {
        subscriptions.clear();
        subscriberSubscriptions.clear();
    }

}
