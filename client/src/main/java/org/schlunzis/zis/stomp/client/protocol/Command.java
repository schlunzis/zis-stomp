package org.schlunzis.zis.stomp.client.protocol;

public enum Command {

    // Client Commands
    SEND,
    SUBSCRIBE,
    UNSUBSCRIBE,
    BEGIN,
    COMMIT,
    ABORT,
    ACK,
    NACK,
    DISCONNECT,
    CONNECT,
    STOMP,

    // Server Commands
    CONNECTED,
    MESSAGE,
    RECEIPT,
    ERROR

}
