package org.schlunzis.zis.stomp.broker.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;
import org.schlunzis.zis.stomp.common.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Optional;

public class StompBrokerImpl<SESSION extends WebsocketSession> implements StompBroker<SESSION> {

    private static final Logger log = LoggerFactory.getLogger(StompBrokerImpl.class);

    private final FrameEncoder frameEncoder = new FrameEncoder();
    private final FrameDecoder frameDecoder = new FrameDecoder();

    public StompBrokerImpl() {
    }

    @Override
    public void start() {
    }

    @Override
    public void onOpen(SESSION session) {
        log.info(session.toString());
    }

    @Override
    public void onMessage(SESSION session, Reader message) {
        log.info(session.toString());
    }

    @Override
    public void onError(SESSION session, @Nullable Throwable t) {
        log.info(session.toString());
    }

    @Override
    public void onClose(SESSION session) {
        log.info(session.toString());
    }

    @Override
    public void close() {
    }

    private Optional<Frame> decodeOrSendError(SESSION session, Reader message) {
        try {
            Frame frame = frameDecoder.decode(message);
            return Optional.of(frame);
        } catch (DecodingException e) {
            Frame errorFrame = Frame.builder()
                    .command(Command.ERROR)
                    .body("Line: " + e.getLine() + ": " + e.getMessage())
                    .build();
            String errorMessage = frameEncoder.encode(errorFrame);
            session.send(errorMessage);
            return Optional.empty();
        }
    }

}
