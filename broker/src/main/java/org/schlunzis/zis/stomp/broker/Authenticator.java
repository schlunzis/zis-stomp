package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;

/// Interface for an authenticator that the [StompBroker] uses.
///
/// @see Authenticator#authenticate(String, String)
/// @since 1.0.0
@FunctionalInterface
public interface Authenticator {

    /// Called by the [StompBroker] to determine weather a client provided a valid login and passcode in the CONNECT
    /// frame.
    ///
    /// The login and passcode are `null`, if none were provided.
    ///
    /// @param login    the login passed by the client
    /// @param passcode the passcode passed by the client
    /// @return `true`, if the client has been authenticated. `false` otherwise.
    /// @since 1.0.0
    boolean authenticate(@Nullable String login, @Nullable String passcode);

}
