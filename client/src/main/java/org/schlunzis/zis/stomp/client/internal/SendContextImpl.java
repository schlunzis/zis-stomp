package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.*;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.protocol.HeadersImpl;

import java.util.Objects;

public final class SendContextImpl implements SendContext {

    private final ReceiptManager receiptManager;
    private final MessageConverter messageConverter;

    private final String destination;
    private final Object body;
    private final Headers headers = new HeadersImpl();

    SendContextImpl(ReceiptManager receiptManager, MessageConverter messageConverter, String destination, Object body) {
        this.receiptManager = receiptManager;
        this.messageConverter = messageConverter;
        this.destination = destination;
        this.body = body;
    }

    @Override
    public SendContext header(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.headers.addFirst(key, value);
        return this;
    }

    @Override
    public void send() throws SendException {
        String stringBody;
        if (body instanceof String s) {
            stringBody = s;
        } else {
            stringBody = messageConverter.convertToString(body);
        }

        FrameBuilder builder = Frame.builder()
                .command(Command.SEND)
                .header("destination", destination)
                .header("content-type", messageConverter.contentType())
                .headers(headers)
                .body(stringBody);

        Frame frame = builder.build();
        receiptManager.sendAndAwaitReceiptIfPolicy(frame, ReceiptPolicy.Policy.FOR_SEND);
    }

}
