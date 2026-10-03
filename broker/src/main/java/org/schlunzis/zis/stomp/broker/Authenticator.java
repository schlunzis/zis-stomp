package org.schlunzis.zis.stomp.broker;

/// TODO
@FunctionalInterface
public interface Authenticator {

    /// TODO
    ///
    /// @param login    asdkjh
    /// @param passcode asgj
    /// @return sgfjkb
    boolean authenticate(String login, String passcode);

}
