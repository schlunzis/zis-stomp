package org.schlunzis.zis.stomp.client.internal.interaction;

import org.schlunzis.zis.stomp.client.SubscribeContext;

import java.util.function.Consumer;

/// This internal class provides the implementation for the [SubscribeContext] used to subscribe to a destination.
///
/// The public part of the API is defined by the [SubscribeContext] interface.
///
/// @see org.schlunzis.zis.stomp.client.StompClient#subscribe(SubscribeContext)
public final class SubscribeContextImpl<T> extends AbstractInteractionContext<SubscribeContext<T>> implements SubscribeContext<T> {

    private final String destination;
    private final Class<T> payloadType;
    private final Consumer<T> messageHandler;

    public SubscribeContextImpl(String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        this.destination = destination;
        this.payloadType = payloadType;
        this.messageHandler = messageHandler;
    }

    @Override
    public String destination() {
        return destination;
    }

    @Override
    public Class<T> payloadType() {
        return payloadType;
    }

    @Override
    public Consumer<T> messageHandler() {
        return messageHandler;
    }

}
