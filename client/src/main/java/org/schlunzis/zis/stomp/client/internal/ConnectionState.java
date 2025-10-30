package org.schlunzis.zis.stomp.client.internal;

/**
 * Enum for the connection state of the client.
 *
 * @since 1.0.0
 */
enum ConnectionState {
    /**
     * Client has not been used yet.
     * It has not connected before.
     *
     * @since 1.0.0
     */
    UNUSED,
    /**
     * Client is in the process of connecting.
     *
     * @since 1.0.0
     */
    CONNECTING,
    /**
     * Client is connected and ready to use.
     * This is the only state where sending and subscribing is allowed.
     *
     * @since 1.0.0
     */
    CONNECTED,
    /**
     * Client is in the process of disconnecting.
     *
     * @since 1.0.0
     */
    DISCONNECTING,
    /**
     * Client is disconnected.
     * No further operations are allowed.
     *
     * @since 1.0.0
     */
    DISCONNECTED
}
