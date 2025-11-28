package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.internal.interaction.EmptyInteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboundChannelTest {

    OutboundChannel outboundChannel;

    @BeforeEach
    void setUp() {
        outboundChannel = new OutboundChannel();
    }

    @Test
    void testHandleWithoutHandler() {
        InteractionContext<?> context = new EmptyInteractionContext<>();
        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        assertDoesNotThrow(() -> outboundChannel.handle(frameBuilder, context));
    }

    @Test
    void testHandleWithHandler() {
        WebSocketClient client = mock(WebSocketClient.class);
        AbstractOutboundChannelHandler handler = new OutboundWebsocketSenderHandler(client);
        outboundChannel.setFirstHandler(handler);

        InteractionContext<?> context = new EmptyInteractionContext<>();
        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        assertDoesNotThrow(() -> outboundChannel.handle(frameBuilder, context));

        verify(client).send(frameBuilder.build());
    }

    @Test
    void testCloseWithoutHandler() {
        assertDoesNotThrow(() -> outboundChannel.close());
    }

    @Test
    void testCloseWithHandler() {
        AbstractOutboundChannelHandler handler = mock(ReceiptOutboundChannelHandler.class);
        outboundChannel.setFirstHandler(handler);

        assertDoesNotThrow(() -> outboundChannel.close());

        verify(handler).close();
    }

}
