package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;

/// TODO
@FunctionalInterface
public interface Authenticator {

    /// TODO
    ///
    /// @param login    asdkjh
    /// @param passcode asgj
    /// @return sgfjkb
    boolean authenticate(@Nullable String login, @Nullable String passcode);

}
