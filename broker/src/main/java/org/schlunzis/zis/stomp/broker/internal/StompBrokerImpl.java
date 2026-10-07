package org.schlunzis.zis.stomp.broker.internal;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.Authenticator;
import org.schlunzis.zis.stomp.broker.StompBroker;
import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;
import org.schlunzis.zis.stomp.broker.util.SessionSet;
import org.schlunzis.zis.stomp.common.Headers;
import org.schlunzis.zis.stomp.common.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StompBrokerImpl<SESSION> implements StompBroker<SESSION> {

    private static final Logger log = LoggerFactory.getLogger(StompBrokerImpl.class);
    private static final String STOMP_VERSION = "1.2";
    private static final String SERVER_NAME = "ZIS";

    private final String[] hosts;
    private final Authenticator authenticator;
    private final StompSessionAdapter<SESSION> sessionAdapter;

    private final FrameEncoder frameEncoder = new FrameEncoder();
    private final FrameDecoder frameDecoder = new FrameDecoder();
    private final SubscriptionStore<SESSION> subscriptionStore;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    private final SessionSet<SESSION> authenticatedSessions;

    public StompBrokerImpl(String[] hosts, Authenticator authenticator, StompSessionAdapter<SESSION> sessionAdapter) {
        this.hosts = hosts;
        this.authenticator = authenticator;
        this.sessionAdapter = sessionAdapter;
        this.subscriptionStore = new SubscriptionStore<>();
        this.authenticatedSessions = new SessionSet<>(sessionAdapter);
    }

    @Override
    public void start() {
        log.info("Started Stomp Broker");
    }

    @Override
    public void onOpen(SESSION session) {
        Objects.requireNonNull(session);
        log.debug("Opened Session: {}", session);
    }

    @Override
    public void onMessage(SESSION session, Reader message) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(message);
        Optional<Frame> optFrame = decodeOrSendError(session, message);
        if (optFrame.isEmpty()) return;
        Frame frame = optFrame.get();
        log.debug("Received Message: {}", frame);

        switch (frame.command()) {
            case STOMP, CONNECT -> onConnect(session, frame);
            case SEND -> onSend(session, frame);
            case SUBSCRIBE -> onSubscribe(session, frame);
            case UNSUBSCRIBE -> onUnsubscribe(session, frame);
            case BEGIN, COMMIT, ABORT, ACK, NACK -> sendErrorAndClose(session, "Command not supported");
            case DISCONNECT -> onDisconnect(session, frame);

            case CONNECTED, MESSAGE, RECEIPT, ERROR -> sendErrorAndClose(session, "Received server frame!");
        }
    }

    private boolean isAuthenticated(SESSION session) {
        return authenticatedSessions.contains(session);
    }

    private void onConnect(SESSION session, Frame connectFrame) {
        String host = connectFrame.headers().getFirst(Headers.HOST);
        if (host == null || !allowsHost(host)) {
            sendErrorAndClose(session, "Host not accepted!");
            return;
        }

        String acceptVersion = connectFrame.headers().getFirst(Headers.ACCEPT_VERSION);
        if (acceptVersion == null || !containsVersion(acceptVersion.split(","))) {
            sendErrorAndClose(session, "No supported version listed!");
            return;
        }

        String login = connectFrame.headers().getFirst(Headers.LOGIN);
        String passcode = connectFrame.headers().getFirst(Headers.PASSCODE);
        if (!authenticator.authenticate(login, passcode)) {
            sendErrorAndClose(session, "Invalid login or passcode!");
            return;
        }

        wLock.lock();
        try {
            authenticatedSessions.add(session);
        } finally {
            wLock.unlock();
        }
        Frame connectedFrame = Frame.builder()
                .command(Command.CONNECTED)
                .header(Headers.VERSION, STOMP_VERSION)
                .header(Headers.SERVER, SERVER_NAME)
                .build();
        send(session, connectedFrame);
    }

    private boolean allowsHost(String host) {
        for (String h : hosts)
            if (h.equals(host))
                return true;
        return false;
    }

    private boolean containsVersion(String[] acceptVersion) {
        for (String av : acceptVersion)
            if (av.equals(STOMP_VERSION))
                return true;
        return false;
    }

    private void onSend(SESSION session, Frame sendFrame) {
        if (!isAuthenticated(session)) {
            sendErrorAndClose(session, "Not authenticated!");
            return;
        }

        String destination = sendFrame.headers().getFirst(Headers.DESTINATION);
        if (destination == null) {
            sendErrorAndClose(session, "No destination given for SEND frame!");
            return;
        }

        FrameBuilder messageFrameBuilder = Frame.builder()
                .command(Command.MESSAGE)
                .body(sendFrame.body().orElse(null));
        Frame messageFrame = messageFrameBuilder.build();

        rLock.lock();
        try {
            subscriptionStore.forEachWithDestination(destination, s -> {
                messageFrame.headers().put(Headers.SUBSCRIPTION, List.of(s.id()));
                messageFrame.headers().put(Headers.MESSAGE_ID, List.of(UUID.randomUUID().toString()));
                send(s.session(), messageFrame);
            });
        } finally {
            rLock.unlock();
        }
        sendReceiptIfRequested(session, sendFrame);
    }

    private void onSubscribe(SESSION session, Frame subscribeFrame) {
        if (!isAuthenticated(session)) {
            sendErrorAndClose(session, "Not authenticated!");
            return;
        }

        String destination = subscribeFrame.headers().getFirst(Headers.DESTINATION);
        if (destination == null) {
            sendErrorAndClose(session, "No destination given for SUBSCRIBE frame!");
            return;
        }

        String id = subscribeFrame.headers().getFirst(Headers.ID);
        if (id == null) {
            sendErrorAndClose(session, "No id given for SUBSCRIBE frame!");
            return;
        }

        wLock.lock();
        try {
            subscriptionStore.add(session, destination, id);
        } finally {
            wLock.unlock();
        }
        sendReceiptIfRequested(session, subscribeFrame);
    }

    private void onUnsubscribe(SESSION session, Frame unsubscribeFrame) {
        if (!isAuthenticated(session)) {
            sendErrorAndClose(session, "Not authenticated!");
            return;
        }

        String id = unsubscribeFrame.headers().getFirst(Headers.ID);
        if (id == null) {
            sendErrorAndClose(session, "No id given for UNSUBSCRIBE frame!");
            return;
        }

        wLock.lock();
        try {
            subscriptionStore.remove(session, id);
        } finally {
            wLock.unlock();
        }
        sendReceiptIfRequested(session, unsubscribeFrame);
    }

    private void onDisconnect(SESSION session, Frame disconnectFrame) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(disconnectFrame);
        sendReceiptIfRequested(session, disconnectFrame);
    }

    @Override
    public void onError(SESSION session, @Nullable Throwable t) {
        Objects.requireNonNull(session);
        log.info("Error: {}", session, t);
        wLock.lock();
        try {
            authenticatedSessions.remove(session);
            subscriptionStore.removeAllFromSession(session);
        } finally {
            wLock.unlock();
        }
    }

    @Override
    public void onClose(SESSION session) {
        Objects.requireNonNull(session);
        log.debug("Closed Session: {}", session);
        wLock.lock();
        try {
            authenticatedSessions.remove(session);
            subscriptionStore.removeAllFromSession(session);
        } finally {
            wLock.unlock();
        }
    }

    @Override
    public void close() {
        log.info("Closing STOMP broker");
        wLock.lock();
        try {
            authenticatedSessions.clear();
        } finally {
            wLock.unlock();
        }
    }

    private Optional<Frame> decodeOrSendError(SESSION session, Reader message) {
        try {
            Frame frame = frameDecoder.decode(message);
            return Optional.of(frame);
        } catch (DecodingException e) {
            sendErrorAndClose(session, "Line: " + e.getLine() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private void sendErrorAndClose(SESSION session, String message) {
        log.debug("Sending error: {}", message);
        Frame errorFrame = Frame.builder()
                .command(Command.ERROR)
                .header("message", message)
                .build();
        send(session, errorFrame);
        close(session);
    }

    private void sendReceiptIfRequested(SESSION session, Frame clientFrame) {
        String receiptId = clientFrame.headers().getFirst(Headers.RECEIPT);
        if (receiptId != null) {
            log.debug("Receipt requested by: {}", clientFrame);
            Frame receiptFrame = Frame.builder()
                    .command(Command.RECEIPT)
                    .header(Headers.RECEIPT_ID, receiptId)
                    .build();
            send(session, receiptFrame);
        } else {
            log.debug("No receipt requested by: {}", clientFrame);
        }
    }

    private void send(SESSION session, Frame frame) {
        log.debug("Sending: {}", frame);
        try {
            String message = frameEncoder.encode(frame);
            sessionAdapter.send(session, message);
        } catch (Throwable t) {
            log.error("Could not send frame!", t);
        }
    }

    private void close(SESSION session) {
        try {
            sessionAdapter.close(session);
        } catch (Throwable t) {
            log.error("Could not close session!", t);
        }
    }

}
