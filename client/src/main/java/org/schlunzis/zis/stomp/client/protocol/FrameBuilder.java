package org.schlunzis.zis.stomp.client.protocol;

import org.jspecify.annotations.Nullable;

public final class FrameBuilder {

    @Nullable
    private Command command;
    private final Headers headers = new Headers();
    private String body = "";

    public FrameBuilder command(Command command) {
        this.command = command;
        return this;
    }

    public FrameBuilder header(String key, String value) {
        this.headers.add(key, value);
        return this;
    }

    public FrameBuilder headers(Headers headers) {
        this.headers.putAll(headers);
        return this;
    }

    public FrameBuilder body(String body) {
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
