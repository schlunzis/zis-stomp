package org.schlunzis.zis.stomp.client.protocol;

/// Exception thrown when there is an error decoding a STOMP frame.
///
/// @see FrameDecoder
public final class DecodingException extends Exception {

    private final String line;

    public DecodingException(String line, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
    }

    public DecodingException(String line, String message) {
        super(message);
        this.line = line;
    }

    public String getLine() {
        return line;
    }

}
