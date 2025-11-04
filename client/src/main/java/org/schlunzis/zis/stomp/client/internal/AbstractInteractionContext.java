package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.protocol.HeadersImpl;

import java.util.Objects;

abstract class AbstractInteractionContext<T> {

    protected final Headers headers = new HeadersImpl();

    public T header(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.headers.addFirst(key, value);
        //noinspection unchecked
        return (T) this;
    }

}
