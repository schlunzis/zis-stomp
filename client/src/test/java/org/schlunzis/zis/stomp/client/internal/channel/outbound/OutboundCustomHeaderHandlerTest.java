package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schlunzis.zis.stomp.client.internal.interaction.EmptyInteractionContext;
import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.common.Headers;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;
import org.schlunzis.zis.stomp.common.protocol.FrameBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboundCustomHeaderHandlerTest {

    OutboundCustomHeaderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OutboundCustomHeaderHandler();
    }

    @Test
    void testHandle() {
        InteractionContext<?> context = new EmptyInteractionContext<>();
        context.header("X-Custom-Header", "Value1");
        context.header("X-Custom-Header", "Value2");
        context.header("X-Another-Header", "AnotherValue");

        FrameBuilder builder = Frame.builder()
                .header("X-Yet-Another-Header", "YetAnotherValue")
                .command(Command.MESSAGE);

        handler.handle(builder, context);

        Headers headers = builder.build().headers();
        assertEquals(2, headers.get("X-Custom-Header").size());
        assertEquals("Value2", headers.get("X-Custom-Header").getFirst());
        assertEquals("Value1", headers.get("X-Custom-Header").get(1));
        assertEquals(1, headers.get("X-Another-Header").size());
        assertEquals("AnotherValue", headers.get("X-Another-Header").getFirst());
        assertEquals(1, headers.get("X-Yet-Another-Header").size());
        assertEquals("YetAnotherValue", headers.get("X-Yet-Another-Header").getFirst());
    }

    @Test
    void testHandleNoCustomHeaders() {
        InteractionContext<?> context = new EmptyInteractionContext<>();

        FrameBuilder builder = Frame.builder()
                .header("X-Yet-Another-Header", "YetAnotherValue")
                .command(Command.MESSAGE);

        handler.handle(builder, context);

        Headers headers = builder.build().headers();
        assertEquals(1, headers.get("X-Yet-Another-Header").size());
        assertEquals("YetAnotherValue", headers.get("X-Yet-Another-Header").getFirst());
    }

    @Test
    void testHandleAddToExistingHeaders() {
        InteractionContext<?> context = new EmptyInteractionContext<>();
        context.header("X-Custom-Header", "NewValue");

        FrameBuilder builder = Frame.builder()
                .header("X-Custom-Header", "OldValue")
                .command(Command.MESSAGE);

        handler.handle(builder, context);

        Headers headers = builder.build().headers();
        assertEquals(2, headers.get("X-Custom-Header").size());
        assertEquals("NewValue", headers.get("X-Custom-Header").getFirst());
        assertEquals("OldValue", headers.get("X-Custom-Header").get(1));
    }

}
