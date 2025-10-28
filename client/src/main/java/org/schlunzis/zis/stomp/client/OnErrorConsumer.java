package org.schlunzis.zis.stomp.client;

import java.util.function.BiConsumer;

/**
 * Consumer for handling STOMP ERROR frames.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface OnErrorConsumer extends BiConsumer<String, Message<String>> {

    /**
     * Accepts an error that occurred during STOMP communication.
     *
     * @param message the error message from the ERROR frame
     * @since 1.0.0
     */
    void accept(String m, Message<String> message);

}
