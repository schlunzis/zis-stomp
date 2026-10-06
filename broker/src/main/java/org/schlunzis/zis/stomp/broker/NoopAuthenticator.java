package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;

/// Authenticator that always returns true, meaning all connections are accepted.
///
/// @since 1.0.0
public class NoopAuthenticator implements Authenticator {

    /// Creates a new NoopAuthenticator.
    ///
    /// @since 1.0.0
    public NoopAuthenticator() {
    }

    @Override
    public boolean authenticate(@Nullable String login, @Nullable String passcode) {
        return true;
    }

}
