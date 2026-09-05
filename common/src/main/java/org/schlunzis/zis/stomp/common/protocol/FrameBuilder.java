package org.schlunzis.zis.stomp.common.protocol;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.common.Headers;

import java.util.Objects;
import java.util.Optional;

/// Utility class for creating frames.
///
/// @since 1.0.0
public final class FrameBuilder {

    @Nullable
    private Command command;
    private final Headers headers = new HeadersImpl();
    @Nullable
    private String body = null;

    /// Creates a new frame builder.
    public FrameBuilder() {
    }

    /// Sets the command.
    ///
    /// @param command the command
    /// @return this
    public FrameBuilder command(Command command) {
        Objects.requireNonNull(command, "command must not be null");
        this.command = command;
        return this;
    }

    /// Returns the set command.
    /// Must be called after the command has been set.
    ///
    /// @return the previously set command.
    /// @throws IllegalStateException if the command has not been set.
    public Command command() {
        if (command == null) {
            throw new IllegalStateException("Command has not been set");
        }
        return command;
    }

    /// Adds a header line.
    ///
    /// @param key   the left side of the header
    /// @param value the right side of the header
    /// @return this
    public FrameBuilder header(String key, String value) {
        Objects.requireNonNull(key, "header key must not be null");
        Objects.requireNonNull(value, "header value must not be null");
        this.headers.addFirst(key, value);
        return this;
    }


    /// Extends the headers in this builder by the given headers.
    ///
    /// @param headers the headers to extend with.
    /// @return this
    public FrameBuilder headers(Headers headers) {
        this.headers.putAll(headers);
        return this;
    }

    /// Sets the body.
    ///
    /// @param body the body
    /// @return this
    public FrameBuilder body(@Nullable String body) {
        this.body = body;
        return this;
    }

    /// Builds the frame.
    ///
    /// @return the frame with the set values.
    /// @throws IllegalStateException if no command has been set.
    public Frame build() {
        if (command == null) {
            throw new IllegalStateException("Command must be set");
        }

        return new Frame(command, headers, Optional.ofNullable(body));
    }

}
