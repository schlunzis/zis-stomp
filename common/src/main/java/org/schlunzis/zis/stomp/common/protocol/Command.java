package org.schlunzis.zis.stomp.common.protocol;

/// Enum representing STOMP commands used in the STOMP protocol.
///
/// This enum includes both client and server commands.
/// All commands must have the same name as defined in the STOMP protocol specification as they are used to encode
/// and decode STOMP frames.
public enum Command {

    // Client Commands

    /// SEND
    SEND,
    /// SUBSCRIBE
    SUBSCRIBE,
    /// UNSUBSCRIBE
    UNSUBSCRIBE,
    /// BEGIN
    BEGIN,
    /// COMMIT
    COMMIT,
    /// ABORT
    ABORT,
    /// ACK
    ACK,
    /// NACK
    NACK,
    /// DISCONNECT
    DISCONNECT,
    /// CONNECT
    CONNECT,
    /// STOMP
    STOMP,

    // Server Commands

    /// CONNECTED
    CONNECTED,
    /// MESSAGE
    MESSAGE,
    /// RECEIPT
    RECEIPT,
    /// ERROR
    ERROR

}
