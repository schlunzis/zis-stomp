package org.schlunzis.zis.stomp.broker;

public class NoopAuthenticator implements Authenticator {

    @Override
    public boolean authenticate(String login, String passcode) {
        return true;
    }

}
