package org.schlunzis.zis.stomp.broker.util;

import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;

import java.util.HashMap;
import java.util.Map;

public class SessionSet<SESSION> {

    private final Map<Integer, SESSION> elements = new HashMap<>();
    private final StompSessionAdapter<SESSION> sessionAdapter;

    public SessionSet(StompSessionAdapter<SESSION> sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    public void add(SESSION session) {
        int hash = sessionAdapter.hashCode(session);
        elements.put(hash, session);
    }

    public void remove(SESSION session) {
        int hash = sessionAdapter.hashCode(session);
        elements.remove(hash);
    }

    public boolean contains(SESSION session) {
        int hash = sessionAdapter.hashCode(session);
        return elements.containsKey(hash);
    }

    public void clear() {
        elements.clear();
    }

}
