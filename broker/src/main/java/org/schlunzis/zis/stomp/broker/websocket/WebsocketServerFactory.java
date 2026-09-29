package org.schlunzis.zis.stomp.broker.websocket;

import java.io.Reader;
import java.util.function.Consumer;

@FunctionalInterface
public interface WebsocketServerFactory {

    WebsocketServer create(Consumer<Reader> frameConsumer);

}
