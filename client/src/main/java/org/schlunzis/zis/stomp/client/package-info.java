/**
 * This package contains the public API for the STOMP client.
 * <p>
 * You can acquire a STOMP client instance via the {@link org.schlunzis.zis.stomp.client.StompClient#builder()} method.
 * You may also register a custom {@link org.schlunzis.zis.stomp.client.MessageConverter} to handle message
 * conversion, e.g., for JSON payloads. Default implementations for Jackson 2 and Jackson 3 are provided.
 * They are automatically used if the respective Jackson version is available.
 */
package org.schlunzis.zis.stomp.client;
