package org.schlunzis.zis.stomp.client.protocol;

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
