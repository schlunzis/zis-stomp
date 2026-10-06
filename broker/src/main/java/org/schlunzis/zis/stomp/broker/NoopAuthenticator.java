package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;

/// Authenticator that always returns true, meaning all connections are accepted.
public class NoopAuthenticator implements Authenticator {

    /// Creates a new NoopAuthenticator.
    public NoopAuthenticator() {
    }

    @Override
    public boolean authenticate(@Nullable String login, @Nullable String passcode) {
        return true;
    }

}
