package org.schlunzis.zis.stomp.client.protocol;

import java.nio.charset.StandardCharsets;

public final class FrameEncoder {

    /**
     * Encodes a Frame into its STOMP string representation.
     * <p>
     * It automatically adds the "content-length" header if the frame has a body and the command is SEND, MESSAGE, or
     * ERROR; i.e., commands that support a body.
     *
     * @param frame the Frame to encode
     * @return the encoded STOMP string representation of the Frame
     */
    public String encode(Frame frame) {
        final StringBuilder builder = new StringBuilder();

        // Command
        builder.append(frame.command().name()).append('\n');

        // Headers
        frame.headers().forEach((key, values) -> {
            for (String value : values) {
                builder.append(key).append(':').append(escapeHeaderValue(value)).append('\n');
            }
        });

        // Body
        if (frame.body().isPresent() &&
                (frame.command() == Command.SEND ||
                        frame.command() == Command.MESSAGE ||
                        frame.command() == Command.ERROR)) {
            builder.append("content-length:").append(frame.body().get().getBytes(StandardCharsets.UTF_8).length).append('\n');
            builder.append('\n');
            builder.append(frame.body().get());
        } else {
            builder.append('\n');
        }

        // Frame terminator
        builder.append('\0');
        return builder.toString();
    }

    /**
     * Escapes special characters in header values according to the STOMP protocol.
     *
     * @param value the header value to escape
     * @return the escaped header value
     */
    private String escapeHeaderValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace(":", "\\c");
    }

}
