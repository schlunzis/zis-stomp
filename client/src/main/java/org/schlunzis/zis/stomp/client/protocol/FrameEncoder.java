package org.schlunzis.zis.stomp.client.protocol;

import java.nio.charset.StandardCharsets;

public final class FrameEncoder {

    public String encode(Frame frame) {
        final StringBuilder builder = new StringBuilder();
        builder.append(frame.command().name()).append('\n');
        frame.headers().forEach((key, values) -> {
            for (String value : values) {
                builder.append(key).append(':').append(escapeHeaderValue(value)).append('\n');
            }
        });
        if (!frame.body().isEmpty() &&
                (frame.command() == Command.SEND ||
                        frame.command() == Command.MESSAGE ||
                        frame.command() == Command.ERROR)) {
            builder.append("content-length:").append(frame.body().getBytes(StandardCharsets.UTF_8).length).append('\n');
            builder.append('\n');
            builder.append(frame.body());
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
