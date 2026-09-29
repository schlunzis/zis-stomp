package org.schlunzis.zis.stomp.broker.internal;

import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketServer;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketServerFactory;
import org.schlunzis.zis.stomp.common.protocol.DecodingException;
import org.schlunzis.zis.stomp.common.protocol.Frame;
import org.schlunzis.zis.stomp.common.protocol.FrameDecoder;
import org.schlunzis.zis.stomp.common.protocol.FrameEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;

public class StompBrokerImpl implements StompBroker {

    private static final Logger log = LoggerFactory.getLogger(StompBrokerImpl.class);

    private final WebsocketServer server;
    private final FrameEncoder frameEncoder = new FrameEncoder();
    private final FrameDecoder frameDecoder = new FrameDecoder();

    public StompBrokerImpl(WebsocketServerFactory serverFactory) {
        this.server = serverFactory.create(this::handleFrame);
    }

    private void handleFrame(Reader frameReader) { // FIXME add some kind of session id
        try {
            Frame frame = frameDecoder.decode(frameReader);
        } catch (DecodingException e) {
            log.error("Could not decode received frame!", e);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void close() {
        server.close();
    }

}
