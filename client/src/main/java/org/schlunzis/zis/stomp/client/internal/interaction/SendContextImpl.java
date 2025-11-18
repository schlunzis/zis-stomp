package org.schlunzis.zis.stomp.client.internal.interaction;

import org.schlunzis.zis.stomp.client.SendContext;

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
