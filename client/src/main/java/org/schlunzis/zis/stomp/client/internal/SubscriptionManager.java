package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/// Manages STOMP subscriptions, including creation, message handling, and removal.
///
/// Subscriptions can be created either programmatically or via annotated subscriber methods.
///
/// Programmatically created subscriptions are simply created with a destination and a SubscriberInvoker.
/// Annotated subscriptions are created by scanning an object for methods annotated with the
/// [org.schlunzis.zis.stomp.client.Topic] annotation, and creating subscriptions for each such method.
/// The scanning for those methods is handled by the [AnnotatedSubscriberHandler].
///
/// @see StompSubscription
/// @see SubscriberInvoker
public class SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);

    private final Map<UUID, StompSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<Object, Set<StompSubscription>> subscriberSubscriptions = Collections.synchronizedMap(new IdentityHashMap<>());

    private final AnnotatedSubscriberHandler annotatedSubscriberHandler;

    SubscriptionManager(AnnotatedSubscriberHandler annotatedSubscriberHandler) {
        this.annotatedSubscriberHandler = annotatedSubscriberHandler;
    }

    /// Creates subscriptions for all annotated methods in the given subscriber object.
    ///
    /// It registers the created subscriptions internally. As soon as the method returns,
    /// the subscriptions are active and can receive messages, if the SUBSCRIBE frames have been sent.
    ///
    /// @param subscriber The subscriber object containing annotated methods.
    /// @return A set of [StompSubscription] instances created from the annotated methods.
    Set<StompSubscription> createAnnotatedSubscriptions(Object subscriber) {
        Set<StompSubscription> s = new HashSet<>(annotatedSubscriberHandler.handle(subscriber));
        subscriberSubscriptions.put(subscriber, s);
        return s;
    }

    /// Creates a new subscription for the given destination and subscriber invoker.
    ///
    /// The created subscription is registered internally and is active as soon as the method returns.
    /// The caller is responsible for sending the SUBSCRIBE frame to the STOMP server.
    ///
    /// @param destination The STOMP destination to subscribe to.
    /// @param invoker     The subscriber invoker to handle incoming messages.
    /// @return The created [StompSubscription] instance.
    StompSubscription create(String destination, SubscriberInvoker<?> invoker) {
        StompSubscription subscription = new StompSubscription(
                this,
                UUID.randomUUID(),
                destination,
                invoker
        );
        subscriptions.put(subscription.id(), subscription);
        log.trace("Created subscription {} for destination {}", subscription.id(), destination);
        return subscription;
    }

    /// Handles an incoming STOMP frame by routing it to the appropriate subscription.
    ///
    /// @param frame The incoming STOMP frame to handle.
    /// @see org.schlunzis.zis.stomp.client.internal.channel.inbound.SubscriptionsInboundChannelHandler
    public void handleMessage(Frame frame) {
        String subscriptionId = frame.headers().getFirst("subscription");
        if (subscriptionId == null) {
            log.warn("Received frame without subscription id: {}", frame);
            return;
        }

        try {
            UUID id = UUID.fromString(subscriptionId);
            StompSubscription subscription = subscriptions.get(id);
            log.trace("Received frame for subscription {}", subscription);
            if (subscription != null) {
                subscription.invoker().invoke(frame);
            } else {
                log.warn("No subscription found for id {}", subscriptionId);
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.error("Failed to cast subscription for id {}: {}", subscriptionId, e.getMessage());
        }
    }

    /// Removes the given subscription from the manager.
    ///
    /// Messages received for this subscription will no longer be routed.
    /// The caller is responsible for sending the UNSUBSCRIBE frame to the STOMP server.
    ///
    /// @param stompSubscription The subscription to remove.
    void remove(Subscription stompSubscription) {
        subscriptions.remove(stompSubscription.id());
    }

    /// Checks if a subscription with the given ID exists.
    ///
    /// @param id The UUID of the subscription to check.
    /// @return true if the subscription exists, false otherwise.
    boolean contains(UUID id) {
        return subscriptions.containsKey(id);
    }

    /// Checks if there are any subscriptions associated with the given subscriber object.
    ///
    /// @param subscriber The subscriber object to check.
    /// @return true if there are subscriptions for the subscriber, false otherwise.
    boolean hasSubscriptionsForSubscriber(Object subscriber) {
        return subscriberSubscriptions.containsKey(subscriber);
    }

    /// Removes all subscriptions associated with the given subscriber object.
    ///
    /// Messages received for these subscriptions will no longer be routed.
    /// The caller is responsible for sending the UNSUBSCRIBE frames to the STOMP server.
    ///
    /// @param subscriber The subscriber object whose subscriptions should be removed.
    /// @return The set of removed [StompSubscription] instances, or an empty Optional if none were found.
    Optional<Set<StompSubscription>> remove(Object subscriber) {
        Set<StompSubscription> subs = subscriberSubscriptions.remove(subscriber);
        if (subs != null) {
            for (Subscription sub : subs) {
                subscriptions.remove(sub.id());
            }
        }
        return Optional.ofNullable(subs);
    }

    /// Clears all subscriptions from the manager.
    /// After calling this method, no subscriptions will remain active.
    /// This also gives up all references to subscribers and their associated references.
    public void clear() {
        subscriptions.clear();
        subscriberSubscriptions.clear();
    }

}
