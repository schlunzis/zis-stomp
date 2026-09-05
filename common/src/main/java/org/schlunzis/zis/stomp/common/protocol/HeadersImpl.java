package org.schlunzis.zis.stomp.common.protocol;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.common.Headers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/// Implementation of the [Headers] interface using a LinkedHashMap to maintain insertion order.
public final class HeadersImpl extends LinkedHashMap<String, List<String>>
        implements Headers {

    /// Creates an empty header instance.
    public HeadersImpl() {
    }

    @Override
    public void addFirst(String key, String value) {
        this.computeIfAbsent(key, k -> new ArrayList<>(1)).addFirst(value);
    }

    @Override
    public @Nullable String getFirst(String key) {
        List<String> values = this.get(key);
        if (values != null && !values.isEmpty()) {
            return values.getFirst();
        }
        return null;
    }

}
