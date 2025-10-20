package org.schlunzis.zis.stomp.client.protocol;

import java.nio.charset.StandardCharsets;

public final class MessageEncoder {

    public String encode(Message message) {
        final StringBuilder builder = new StringBuilder();
        builder.append(message.command().name()).append('\n');
        message.headers().forEach((key, values) -> {
            for (String value : values) {
                builder.append(key).append(':').append(escapeHeaderValue(value)).append('\n');
            }
        });
        if (!message.body().isEmpty() &&
                (message.command() == Command.SEND ||
                        message.command() == Command.MESSAGE ||
                        message.command() == Command.ERROR)) {
            builder.append("content-length:").append(message.body().getBytes(StandardCharsets.UTF_8).length).append('\n');
            builder.append('\n');
            builder.append(message.body());
        } else {
            builder.append('\n');
        }
        builder.append('\0');
        builder.append('\n');
        return builder.toString();
    }

    private String escapeHeaderValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace(":", "\\c");
    }

}
