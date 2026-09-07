package org.schlunzis.zis.stomp.broker;

@FunctionalInterface
public interface Authenticator {

    boolean authenticate(String login, String passcode);

}
