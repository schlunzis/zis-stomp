package org.schlunzis.zis.stomp.common.protocol;

/// Exception thrown when there is an error decoding a STOMP frame.
///
/// @see FrameDecoder
public final class DecodingException extends Exception {

    /// The line where the error occurred.
    private final String line;

    /// Creates a decoding exception
    ///
    /// @param line    the full line where the error occurred
    /// @param message the message describing what is wrong
    /// @param cause   the exception that caused the error
    public DecodingException(String line, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
    }

    /// Creates a decoding exception
    ///
    /// @param line    the full line where the error occurred
    /// @param message the message describing what is wrong
    public DecodingException(String line, String message) {
        super(message);
        this.line = line;
    }

    /// Returns the full line of where the error occurred.
    ///
    /// @return the line where the error occurred
    public String getLine() {
        return line;
    }

}
