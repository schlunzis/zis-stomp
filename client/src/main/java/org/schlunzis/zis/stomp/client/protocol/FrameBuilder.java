package org.schlunzis.zis.stomp.client.protocol;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Headers;

import java.util.Objects;

public final class FrameBuilder {

    @Nullable
    private Command command;
    private final Headers headers = new HeadersImpl();
    private String body = "";

    public FrameBuilder command(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        this.command = command;
        return this;
    }

    public FrameBuilder header(String key, String value) {
        Objects.requireNonNull(key, "header key must not be null");
        Objects.requireNonNull(value, "header value must not be null");
        this.headers.addFirst(key, value);
        return this;
    }

    public FrameBuilder headers(Headers headers) {
        this.headers.putAll(headers);
        return this;
    }

    public FrameBuilder body(String body) {
        Objects.requireNonNull(body, "body must not be null");
        this.body = body;
        return this;
    }

    public Frame build() {
        if (command == null) {
            throw new IllegalStateException("Command must be set");
        }

        return new Frame(command, headers, body);
    }

}
