package org.schlunzis.zis.stomp.client.subscriptions;

import org.schlunzis.zis.stomp.client.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

    private final Map<UUID, StompSubscription> subscriptions = new ConcurrentHashMap<>();

    public Subscription create(String destination, Consumer<String> consumer) {
        StompSubscription subscription = new StompSubscription(
                this,
                UUID.randomUUID(),
                destination,
                consumer,
                null
        );
        subscriptions.put(subscription.id(), subscription);
        return subscription;
    }

    public void handleMessage(UUID id, String body) {
        try {
            StompSubscription subscription = subscriptions.get(id);
            if (subscription != null) {
                subscription.consumer().accept(body);
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast subscription for id {}: {}", id, e.getMessage());
        }
    }

    public void remove(Subscription stompSubscription) {
        subscriptions.remove(stompSubscription.id());
    }

    public boolean contains(UUID id) {
        return subscriptions.containsKey(id);
    }

}
