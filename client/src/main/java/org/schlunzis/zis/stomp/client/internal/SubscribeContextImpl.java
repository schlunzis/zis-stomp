package org.schlunzis.zis.stomp.client.internal;

import org.schlunzis.zis.stomp.client.MessageConverter;
import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.SubscribeContext;
import org.schlunzis.zis.stomp.client.Subscription;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.function.Consumer;

public final class SubscribeContextImpl<T> extends AbstractInteractionContext<SubscribeContext> implements SubscribeContext {

    private final ReceiptManager receiptManager;
    private final SubscriptionManager subscriptionManager;
    private final MessageConverter messageConverter;

    private final String destination;
    private final Class<T> payloadType;
    private final Consumer<T> messageHandler;

    SubscribeContextImpl(ReceiptManager receiptManager, SubscriptionManager subscriptionManager, MessageConverter messageConverter,
                         String destination, Class<T> payloadType, Consumer<T> messageHandler) {
        this.receiptManager = receiptManager;
        this.subscriptionManager = subscriptionManager;
        this.messageConverter = messageConverter;
        this.destination = destination;
        this.payloadType = payloadType;
        this.messageHandler = messageHandler;
    }

    @Override
    public Subscription subscribe() {
        Subscription subscription = subscriptionManager.create(
                destination,
                new SubscriberInvoker(messageConverter, payloadType, messageHandler)
        );

        Frame frame = Frame.builder()
                .command(Command.SUBSCRIBE)
                .header("destination", destination)
                .header("id", subscription.id().toString())
                .header("ack", "auto")
                .headers(headers)
                .build();

        receiptManager.sendAndAwaitReceiptIfPolicy(frame, ReceiptPolicy.Policy.FOR_SUBSCRIBE);
        return subscription;
    }

}
