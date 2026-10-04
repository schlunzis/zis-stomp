package org.schlunzis.zis.stomp.broker.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.Authenticator;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;
import org.schlunzis.zis.stomp.common.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StompBrokerImpl<SESSION extends WebsocketSession> implements StompBroker<SESSION> {

    private static final Logger log = LoggerFactory.getLogger(StompBrokerImpl.class);

    private final Authenticator authenticator;

    private final FrameEncoder frameEncoder = new FrameEncoder();
    private final FrameDecoder frameDecoder = new FrameDecoder();
    private final SubscriptionStore<SESSION> subscriptionStore = new SubscriptionStore<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    private final Collection<SESSION> nonAuthenticatedSessions = new HashSet<>();
    private final Collection<SESSION> authenticatedSessions = new HashSet<>();

    public StompBrokerImpl(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void start() {
    }

    @Override
    public void onOpen(SESSION session) {
        log.info(session.toString());
        nonAuthenticatedSessions.add(session);
    }

    @Override
    public void onMessage(SESSION session, Reader message) {
        log.info(session.toString());
        Optional<Frame> optFrame = decodeOrSendError(session, message);
        if (optFrame.isEmpty()) return;
        Frame frame = optFrame.get();

        switch (frame.command()) {
            case STOMP, CONNECT -> {
            }
            case SEND -> {
            }
            case SUBSCRIBE -> {
            }
            case UNSUBSCRIBE -> {
            }
            case BEGIN -> {
            }
            case COMMIT -> {
            }
            case ABORT -> {
            }
            case ACK -> {
            }
            case NACK -> {
            }
            case DISCONNECT -> {
              
            }

            case CONNECTED, MESSAGE, RECEIPT, ERROR -> {
                Frame errorFrame = Frame.builder()
                        .command(Command.ERROR)
                        .body("Received server frame!")
                        .build();
                send(session, errorFrame);
                close(session);
            }
        }
    }

    @Override
    public void onError(SESSION session, @Nullable Throwable t) {
        log.info(session.toString(), t);
        subscriptionStore.removeAllFromSession(session);
    }

    @Override
    public void onClose(SESSION session) {
        log.info(session.toString());
        subscriptionStore.removeAllFromSession(session);
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
            send(session, errorFrame);
            close(session);
            return Optional.empty();
        }
    }

    private void send(SESSION session, Frame frame) {
        try {
            String message = frameEncoder.encode(frame);
            session.send(message);
        } catch (Throwable t) {
            log.error("Could not send frame!", t);
        }
    }

    private void close(SESSION session) {
        try {
            session.close();
        } catch (Throwable t) {
            log.error("Could not close session!", t);
        }
    }

}
