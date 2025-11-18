package org.schlunzis.zis.stomp.client.internal.interaction;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.ReceiptTimeoutException;
import org.schlunzis.zis.stomp.client.protocol.HeadersImpl;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract sealed class AbstractInteractionContext<T> implements InteractionContext<T>
        permits EmptyInteractionContext, SendContextImpl, SubscribeContextImpl {

    protected final Headers headers = new HeadersImpl();

    @Nullable
    private CountDownLatch receiptLatch;
    @Nullable
    private Duration receiptTimeout;

    @Override
    public T header(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.headers.addFirst(key, value);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public void receiptLatch(CountDownLatch countDownLatch) {
        this.receiptLatch = countDownLatch;
    }

    @Override
    public void receiptTimeout(Duration duration) {
        this.receiptTimeout = duration;
    }

    @Override
    public void awaitCompletion() {
        if (receiptLatch != null) {
            if (receiptTimeout == null) {
                throw new IllegalStateException("No timeout duration provided"); // this should never happen
            }
            try {
                boolean completed = receiptLatch.await(receiptTimeout.toMillis(), TimeUnit.MILLISECONDS);
                if (!completed) {
                    throw new ReceiptTimeoutException("Receipt not received within timeout of " + receiptTimeout);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for receipt", e);
            }
        }
    }

}
