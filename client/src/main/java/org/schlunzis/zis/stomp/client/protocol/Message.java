package org.schlunzis.zis.stomp.client.protocol;


public record Message(
        Command command,
        Headers headers,
        String body
) {

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

}
