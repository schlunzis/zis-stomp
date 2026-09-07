package org.schlunzis.zis.stomp.broker;

public interface StompBroker extends AutoCloseable {

    void start();

    void close();

}
