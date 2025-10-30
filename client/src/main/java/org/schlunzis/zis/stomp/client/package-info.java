/**
 * This package contains the public API for the STOMP client.
 * <p>
 * You can acquire a STOMP client instance via the {@link org.schlunzis.zis.stomp.client.StompClient#builder()} method.
 * You may also register a custom {@link org.schlunzis.zis.stomp.client.MessageConverter} to handle message
 * conversion, e.g., for JSON payloads. Default implementations for Jackson 2 and Jackson 3 are provided.
 * They are automatically used if the respective Jackson version is available and discoverable via a ServiceLoader.
 * <p>
 * This package also contains annotations for defining STOMP subscribers and publishers:
 * {@link org.schlunzis.zis.stomp.client.StompSubscriber} and {@link org.schlunzis.zis.stomp.client.StompPublisher}.
 * You can use them to annotate classes and interfaces that handle incoming messages or send outgoing messages
 * to specific STOMP destinations.
 *
 * @since 1.0.0
 */
package org.schlunzis.zis.stomp.client;
