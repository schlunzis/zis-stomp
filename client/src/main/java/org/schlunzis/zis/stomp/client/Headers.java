package org.schlunzis.zis.stomp.client;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.protocol.HeadersImpl;

import java.util.List;
import java.util.Map;

public sealed interface Headers extends Map<String, List<String>> permits HeadersImpl {

    void add(String key, String value);

    @Nullable String getFirst(String key);

}
