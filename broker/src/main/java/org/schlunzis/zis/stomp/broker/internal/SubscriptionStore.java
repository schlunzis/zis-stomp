package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class SubscriptionStore<SESSION extends WebsocketSession> {

    private final Map<String, Collection<Subscription<SESSION>>> subscriptions = new HashMap<>();

    public void add(SESSION session, String topic) {
        Collection<Subscription<SESSION>> subs = subscriptions.computeIfAbsent(topic, _ -> new HashSet<>());
        subs.add(new Subscription<>(session));
    }

    public void forEachWithTopic(String topic, Consumer<Subscription<SESSION>> consumer) {
        Collection<Subscription<SESSION>> subs = subscriptions.computeIfAbsent(topic, _ -> new HashSet<>());
        subs.forEach(consumer);
    }

    public void removeAllFromSession(SESSION session) {
        subscriptions.values().forEach(topicList ->
                topicList.removeIf(s ->
                        s.session().equals(session)
                )
        );
    }

}
