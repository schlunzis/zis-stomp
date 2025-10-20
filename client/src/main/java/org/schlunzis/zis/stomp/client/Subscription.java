package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.subscriptions.StompSubscription;

import java.util.UUID;

public sealed interface Subscription
        permits StompSubscription {

    UUID id();

    String destination();

}
