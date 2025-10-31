package org.schlunzis.zis.stomp.client;

import java.util.function.BiConsumer;

/// Consumer for handling STOMP `ERROR` frames.
///
/// This consumer is called by the StompClient when an `ERROR` frame is received from the server.
/// In that case the connection is closed by the server and no further communication is possible.
///
/// You may try to create a new StompClient instance to reconnect.
/// However, it is recommended to investigate the cause of the error first, since
/// it may indicate a serious issue with the STOMP communication.
///
/// @since 1.0.0
@FunctionalInterface
public interface OnErrorConsumer extends BiConsumer<String, Message<String>> {

    /// Accepts an error that occurred during STOMP communication.
    ///
    /// `m` is the message from the server given in the header.
    /// Usually, more information is provided in the body.
    ///
    /// @param m       the error message from the ERROR frame
    /// @param message the complete ERROR frame message including headers and body
    /// @since 1.0.0
    void accept(String m, Message<String> message);

}
