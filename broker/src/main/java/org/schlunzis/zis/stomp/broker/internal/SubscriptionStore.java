package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class SubscriptionStore<SESSION extends WebsocketSession> {

    private final Map<String, Collection<Subscription<SESSION>>> subscriptions = new HashMap<>();

    public void add(SESSION session, String destination, String id) {
        Collection<Subscription<SESSION>> subs = subscriptions.computeIfAbsent(destination, _ -> new HashSet<>());
        subs.add(new Subscription<>(session, id));
    }

    public void forEachWithDestination(String destination, Consumer<Subscription<SESSION>> consumer) {
        Collection<Subscription<SESSION>> subs = subscriptions.computeIfAbsent(destination, _ -> new HashSet<>());
        subs.forEach(consumer);
    }

    public void removeAllFromSession(SESSION session) {
        subscriptions.values().forEach(topicList ->
                topicList.removeIf(s ->
                        s.session().equals(session)
                )
        );
    }

    public void remove(SESSION session, String id) {
        subscriptions.values().forEach(topicList ->
                topicList.removeIf(s ->
                        s.session().equals(session) && s.id().equals(id)
                )
        );
    }

}
