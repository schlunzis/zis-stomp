package org.schlunzis.zis.stomp.client.protocol;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Headers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class HeadersImpl extends LinkedHashMap<String, List<String>>
        implements Headers {

    @Override
    public void addFirst(String key, String value) {
        this.computeIfAbsent(key, k -> new ArrayList<>(1)).add(0, value);
    }

    @Override
    public @Nullable String getFirst(String key) {
        List<String> values = this.get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

}
