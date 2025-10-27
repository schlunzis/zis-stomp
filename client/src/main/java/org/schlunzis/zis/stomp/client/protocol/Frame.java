package org.schlunzis.zis.stomp.client.protocol;


public record Frame(
        Command command,
        Headers headers,
        String body
) {

    public static FrameBuilder builder() {
        return new FrameBuilder();
    }

}
