package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.OnErrorConsumer;
import org.schlunzis.zis.stomp.common.protocol.Command;
import org.schlunzis.zis.stomp.common.protocol.Frame;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundErrorChannelHandlerTest {

    @Mock
    OnErrorConsumer consumer;

    @Test
    void testHandleErrorFrame() {
        InboundErrorChannelHandler handler = new InboundErrorChannelHandler(consumer);
        Frame errorFrame = Frame.builder()
                .command(Command.ERROR)
                .header("message", "Test error message")
                .build();

        handler.handle(errorFrame);

        verify(consumer).accept("Test error message", errorFrame.headers(), errorFrame.body().orElse(null));
    }

    @Test
    void testHandleErrorFrameWithoutMessageHeader() {
        InboundErrorChannelHandler handler = new InboundErrorChannelHandler(consumer);
        Frame errorFrame = Frame.builder()
                .command(Command.ERROR)
                .build();

        handler.handle(errorFrame);

        verify(consumer).accept("Unknown error", errorFrame.headers(), errorFrame.body().orElse(null));
    }

    @Test
    void testHandleNonErrorFrame() {
        InboundErrorChannelHandler handler = new InboundErrorChannelHandler(consumer);
        Frame messageFrame = Frame.builder()
                .command(Command.MESSAGE)
                .build();

        handler.handle(messageFrame);

        verify(consumer, never()).accept(any(), any(), anyString());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testHandleWithNullConsumer() {
        assertThrows(NullPointerException.class, () -> new InboundErrorChannelHandler(null));
    }

    @Test
    void testHandleThrowsExceptionInConsumer() {
        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(consumer).accept(any(), any(), isNull());
        InboundErrorChannelHandler handler = new InboundErrorChannelHandler(consumer);
        Frame errorFrame = Frame.builder()
                .command(Command.ERROR)
                .header("message", "Test error message")
                .build();

        RuntimeException e = assertThrows(RuntimeException.class, () -> handler.handle(errorFrame));
        assertSame(exception, e);
    }

}
