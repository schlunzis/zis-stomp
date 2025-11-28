package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.internal.interaction.EmptyInteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundWebsocketSenderHandlerTest {

    OutboundWebsocketSenderHandler handler;

    @Mock
    WebSocketClient client;

    @BeforeEach
    void setUp() {
        handler = new OutboundWebsocketSenderHandler(client);
    }

    @Test
    void testHandle() {
        InteractionContext<?> context = new EmptyInteractionContext<>();
        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND)
                .header("destination", "/queue/test")
                .body("Test message");

        handler.handle(frameBuilder, context);

        Frame frame = frameBuilder.build();

        verify(client).send(frame);
    }

}
