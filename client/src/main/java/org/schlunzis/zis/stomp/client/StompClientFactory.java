package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.internal.ReceiptManager;
import org.schlunzis.zis.stomp.client.internal.Stomp1dot2Client;
import org.schlunzis.zis.stomp.client.internal.SubscriptionManager;
import org.schlunzis.zis.stomp.client.internal.channel.inbound.*;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundChannel;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundCustomHeaderHandler;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.OutboundWebsocketSenderHandler;
import org.schlunzis.zis.stomp.client.internal.channel.outbound.ReceiptOutboundChannelHandler;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.schlunzis.zis.stomp.client.websocket.jakarta.JakartaWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.ServiceLoader;

public class StompClientFactory {

    private static final Logger log = LoggerFactory.getLogger(StompClientFactory.class);

    public StompClient create(StompClientBuilder builder) {
        final URI endpoint = extractEndpoint(builder);
        final MessageConverter messageConverter = extractMessageConverter(builder);
        final SubscriptionManager subscriptionManager = new SubscriptionManager(messageConverter);
        final ReceiptManager receiptManager = new ReceiptManager(
                builder.receiptTimeout(),
                builder.receiptPolicy()
        );

        final InboundChannel inboundChannel = createInboundChannel(builder, subscriptionManager, receiptManager);
        final WebSocketClient webSocketClient = new JakartaWebsocketClient(endpoint, inboundChannel::handle);
        final OutboundChannel outboundChannel = createOutboundChannel(builder, webSocketClient, receiptManager);

        return new Stomp1dot2Client(endpoint, messageConverter, subscriptionManager, webSocketClient, inboundChannel, outboundChannel);
    }

    private URI extractEndpoint(StompClientBuilder builder) {
        URI endpoint = builder.endpoint();
        if (endpoint == null) {
            throw new IllegalStateException("Endpoint must be set");
        }
        return endpoint;
    }

    private MessageConverter extractMessageConverter(StompClientBuilder builder) {
        MessageConverter messageConverter = builder.messageConverter();
        if (messageConverter != null) {
            log.debug("Using user-provided MessageConverter: {}", messageConverter.getClass().getName());
            return messageConverter;
        }

        return findMessageConverter();
    }

    private MessageConverter findMessageConverter() {
        Class<?> mapperClass;
        boolean jackson3 = false;
        try {
            mapperClass = Class.forName("tools.jackson.databind.ObjectMapper");
            jackson3 = true;
            log.debug("Found Jackson 3 ObjectMapper");
        } catch (ClassNotFoundException _) {
            try {
                mapperClass = Class.forName("com.fasterxml.jackson.core.ObjectCodec");
                log.debug("Found Jackson 2 ObjectMapper");
            } catch (ClassNotFoundException _) {
                log.debug("No Jackson ObjectMapper found");
                return new StringMessageConverter();
            }
        }

        ServiceLoader<?> loader = ServiceLoader.load(mapperClass);
        Optional<?> mapper = loader.findFirst();
        if (mapper.isPresent()) {
            log.debug("Found {} via ServiceLoader", mapperClass);
            if (jackson3) return new Jackson3MessageConverter((tools.jackson.databind.ObjectMapper) mapper.get());
            else return new Jackson2MessageConverter((com.fasterxml.jackson.databind.ObjectMapper) mapper.get());
        }

        log.debug("No MessageConverter found via ServiceLoader");
        return new StringMessageConverter();
    }

    private OutboundChannel createOutboundChannel(StompClientBuilder builder, WebSocketClient webSocketClient, ReceiptManager receiptManager) {
        OutboundChannel outboundChannel = new OutboundChannel();

        ReceiptOutboundChannelHandler receiptOutboundChannelHandler = new ReceiptOutboundChannelHandler(receiptManager);
        outboundChannel.setFirstHandler(receiptOutboundChannelHandler);

        OutboundCustomHeaderHandler customHeaderHandler = new OutboundCustomHeaderHandler();
        receiptOutboundChannelHandler.setNext(customHeaderHandler);

        OutboundWebsocketSenderHandler websocketSenderHandler = new OutboundWebsocketSenderHandler(webSocketClient);
        customHeaderHandler.setNext(websocketSenderHandler);

        return outboundChannel;
    }

    private InboundChannel createInboundChannel(StompClientBuilder builder, SubscriptionManager subscriptionManager, ReceiptManager receiptManager) {
        InboundChannel inboundChannel = new InboundChannel();

        ReceiptInboundChannelHandler receiptInboundChannelHandler = new ReceiptInboundChannelHandler(receiptManager);
        inboundChannel.setFirstHandler(receiptInboundChannelHandler);

        SubscriptionsInboundChannelHandler subscriptionsInboundChannelHandler = new SubscriptionsInboundChannelHandler(
                subscriptionManager
        );
        receiptInboundChannelHandler.setNext(subscriptionsInboundChannelHandler);

        InboundConnectedChannelHandler inboundConnectedChannelHandler = new InboundConnectedChannelHandler();
        inboundConnectedChannelHandler.setConnectedFrameConsumer(inboundChannel::connected);
        subscriptionsInboundChannelHandler.setNext(inboundConnectedChannelHandler);

        OnErrorConsumer onErrorConsumer = builder.onErrorConsumer();
        if (onErrorConsumer != null) {
            InboundErrorChannelHandler inboundErrorChannelHandler = new InboundErrorChannelHandler(onErrorConsumer);
            inboundConnectedChannelHandler.setNext(inboundErrorChannelHandler);
        }

        return inboundChannel;
    }

}
