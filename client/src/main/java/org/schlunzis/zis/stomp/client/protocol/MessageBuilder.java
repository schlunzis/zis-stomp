package org.schlunzis.zis.stomp.client.protocol;

import org.jspecify.annotations.Nullable;

public final class MessageBuilder {

    @Nullable
    private Command command;
    private final Headers headers = new Headers();
    private String body = "";

    public MessageBuilder command(Command command) {
        this.command = command;
        return this;
    }

    public MessageBuilder header(String key, String value) {
        this.headers.add(key, value);
        return this;
    }

    public MessageBuilder headers(Headers headers) {
        this.headers.putAll(headers);
        return this;
    }

    public MessageBuilder body(String body) {
        this.body = body;
        return this;
    }

    public Message build() {
        if (command == null) {
            throw new IllegalStateException("Command must be set");
        }

        return new Message(command, headers, body);
    }

}
