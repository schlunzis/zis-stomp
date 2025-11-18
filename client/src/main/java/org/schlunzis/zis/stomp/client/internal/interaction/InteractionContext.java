package org.schlunzis.zis.stomp.client.internal.interaction;

import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.SendContext;
import org.schlunzis.zis.stomp.client.SubscribeContext;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public sealed interface InteractionContext<T> permits AbstractInteractionContext,
        SendContext, SubscribeContext {

    T header(String key, String value);

    Headers headers();

    void receiptLatch(CountDownLatch countDownLatch);

    void receiptTimeout(Duration duration);

    void awaitCompletion();

}
