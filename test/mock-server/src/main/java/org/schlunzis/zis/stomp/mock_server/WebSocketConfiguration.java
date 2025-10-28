package org.schlunzis.zis.stomp.mock_server;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final ApplicationContext applicationContext;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/server");
        config.enableSimpleBroker("/insight");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }

    // https://stackoverflow.com/a/54937334
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ExecutorChannelInterceptor() {
            @Override
            public void afterMessageHandled(Message<?> inMessage, MessageChannel inChannel, MessageHandler handler, Exception ex) {
                StompHeaderAccessor inAccessor = StompHeaderAccessor.wrap(inMessage);
                String receipt = inAccessor.getReceipt();
                if (receipt == null || receipt.isEmpty()) {
                    return;
                }

                StompHeaderAccessor outAccessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
                outAccessor.setSessionId(inAccessor.getSessionId());
                outAccessor.setReceiptId(receipt);
                outAccessor.setLeaveMutable(true);

                Message<byte[]> outMessage = MessageBuilder.createMessage(new byte[0], outAccessor.getMessageHeaders());

                applicationContext.getBean("clientOutboundChannel", MessageChannel.class).send(outMessage);
            }
        });
    }

}
