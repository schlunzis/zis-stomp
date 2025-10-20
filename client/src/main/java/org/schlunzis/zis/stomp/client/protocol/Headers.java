package org.schlunzis.zis.stomp.client.protocol;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Headers extends LinkedHashMap<String, List<String>>
        implements Map<String, List<String>> {

    public void add(String key, String value) {
        this.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public @Nullable String getFirst(String key) {
        List<String> values = this.get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

}
