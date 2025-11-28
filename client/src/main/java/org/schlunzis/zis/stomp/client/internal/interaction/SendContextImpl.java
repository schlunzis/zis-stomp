package org.schlunzis.zis.stomp.client.internal.interaction;

import org.schlunzis.zis.stomp.client.SendContext;

/// This internal class provides the implementation for the [SendContext] used to send frames with the SEND command.
///
/// The public part of the API is defined by the [SendContext] interface.
///
/// @see org.schlunzis.zis.stomp.client.StompClient#send(SendContext)
public final class SendContextImpl extends AbstractInteractionContext<SendContext> implements SendContext {

    private final String destination;
    private final Object body;

    public SendContextImpl(String destination, Object body) {
        this.destination = destination;
        this.body = body;
    }

    @Override
    public String destination() {
        return destination;
    }

    @Override
    public Object body() {
        return body;
    }

}
