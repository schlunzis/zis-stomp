package org.schlunzis.zis.stomp.common.protocol;

import org.schlunzis.zis.stomp.common.Headers;

import java.util.Objects;
import java.util.Optional;

/// This record represents a STOMP frame consisting of a command, headers, and an optional body.
///
/// A STOMP frame is the fundamental unit of communication in the STOMP protocol, used for sending and receiving messages
/// between clients and servers.
/// If the body is not present, it will be represented as an empty string.
///
/// If the command cannot have a body, the body should be an empty string.
///
/// @param command the STOMP command of the frame
/// @param headers the headers associated with the frame
/// @param body    the optional body of the frame
public record Frame(
        Command command,
        Headers headers,
        Optional<String> body
) {

    /// Do not use this constructor directly. Use the FrameBuilder instead.
    ///
    /// Creates a new Frame instance.
    ///
    /// @param command the STOMP command of the frame
    /// @param headers the headers associated with the frame
    /// @param body    the body of the frame
    public Frame {
        Objects.requireNonNull(command, "command is null");
        Objects.requireNonNull(headers, "headers is null");
        Objects.requireNonNull(body, "body is null");
    }

    /// Creates a new FrameBuilder instance for constructing Frame objects.
    ///
    /// @return a new FrameBuilder
    public static FrameBuilder builder() {
        return new FrameBuilder();
    }

}
