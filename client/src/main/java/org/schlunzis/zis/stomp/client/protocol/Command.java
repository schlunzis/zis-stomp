package org.schlunzis.zis.stomp.client.protocol;

/// Enum representing STOMP commands used in the STOMP protocol.
///
/// This enum includes both client and server commands.
/// All commands must have the same name as defined in the STOMP protocol specification as they are used to encode
/// and decode STOMP frames.
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
