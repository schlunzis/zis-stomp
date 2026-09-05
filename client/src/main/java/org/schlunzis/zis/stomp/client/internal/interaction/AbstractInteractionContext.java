package org.schlunzis.zis.stomp.client.internal.interaction;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.client.internal.Receiptable;
import org.schlunzis.zis.stomp.common.Headers;
import org.schlunzis.zis.stomp.common.protocol.HeadersImpl;

import java.util.Objects;

/// This abstract class provides a base implementation for interaction contexts used in STOMP client operations.
///
/// It provides common functionality for managing headers, receipts and awaiting the interactions completion.
///
/// @param <T> The type of the interaction context, allowing for fluent method chaining.
public abstract sealed class AbstractInteractionContext<T> implements InteractionContext<T>
        permits EmptyInteractionContext, SendContextImpl, SubscribeContextImpl {

    /// Additional headers to be included in the STOMP frame.
    ///
    /// @see InteractionContext#header(String, String)
    /// @see InteractionContext#headers()
    private final Headers headers = new HeadersImpl();

    @Nullable
    private Receiptable receiptable;

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
    public void receiptable(Receiptable receiptable) {
        this.receiptable = receiptable;
    }

    @Override
    @Nullable
    public Receiptable receiptable() {
        return receiptable;
    }

    @Override
    public void awaitCompletion() {
        if (receiptable != null) {
            try {
                receiptable.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for receipt", e);
            }
        }
    }

}
