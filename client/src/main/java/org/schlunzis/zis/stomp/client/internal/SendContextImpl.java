package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.SendContext;
import org.schlunzis.zis.stomp.client.SendException;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

public final class SendContextImpl extends AbstractInteractionContext<SendContext> implements SendContext {

    private final ReceiptManager receiptManager;
    private final MessageConverter messageConverter;

    private final String destination;
    private final Object body;

    SendContextImpl(ReceiptManager receiptManager, MessageConverter messageConverter, String destination, Object body) {
        this.receiptManager = receiptManager;
        this.messageConverter = messageConverter;
        this.destination = destination;
        this.body = body;
    }

    @Override
    public void send() throws SendException {
        String stringBody;
        if (body instanceof String s) {
            stringBody = s;
        } else {
            stringBody = messageConverter.convertToString(body);
        }

        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", messageConverter.contentType())
                .body(stringBody)
                .headers(headers)
                .build();

        receiptManager.sendAndAwaitReceiptIfPolicy(frame, ReceiptPolicy.Policy.FOR_SEND);
    }

}
