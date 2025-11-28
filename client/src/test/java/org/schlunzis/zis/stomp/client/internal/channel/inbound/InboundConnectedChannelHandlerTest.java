package org.schlunzis.zis.stomp.client.internal.channel.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundConnectedChannelHandlerTest {

    @Mock
    Consumer<Frame> consumer;

    @Test
    void testHandle() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        handler.setConnectedFrameConsumer(consumer);
        Frame connectedFrame = Frame.builder()
                .command(Command.CONNECTED)
                .build();

        handler.handle(connectedFrame);

        verify(consumer).accept(connectedFrame);
    }

    @Test
    void testHandleNonConnectedFrame() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        handler.setConnectedFrameConsumer(consumer);
        Frame otherFrame = Frame.builder()
                .command(Command.MESSAGE)
                .build();

        handler.handle(otherFrame);

        verify(consumer, org.mockito.Mockito.never()).accept(otherFrame);
    }

    @Test
    void testHandleWithoutConsumer() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        Frame connectedFrame = Frame.builder()
                .command(Command.CONNECTED)
                .build();

        handler.handle(connectedFrame);

        verify(consumer, never()).accept(connectedFrame);
    }

    @SuppressWarnings({"DataFlowIssue", "WriteOnlyObject"})
    @Test
    void testSetConnectedFrameConsumerWithNull() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();

        assertThrows(NullPointerException.class, () -> handler.setConnectedFrameConsumer(null));
    }

    @Test
    void testHandleThrowingExceptionInConsumer() {
        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(consumer).accept(any());

        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        handler.setConnectedFrameConsumer(consumer);
        Frame connectedFrame = Frame.builder()
                .command(Command.CONNECTED)
                .build();

        RuntimeException e = assertThrows(RuntimeException.class, () -> handler.handle(connectedFrame));

        assertSame(exception, e);
    }

    @Test
    void testHandleMultipleConnectedFrames() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        handler.setConnectedFrameConsumer(consumer);
        Frame connectedFrame1 = Frame.builder()
                .command(Command.CONNECTED)
                .header("key1", "value1")
                .build();
        Frame connectedFrame2 = Frame.builder()
                .command(Command.CONNECTED)
                .header("key2", "value2")
                .build();

        handler.handle(connectedFrame1);
        handler.handle(connectedFrame2);

        verify(consumer).accept(connectedFrame1);
        verify(consumer).accept(connectedFrame2);
    }

    @Test
    void testHandleNoFrames() {
        InboundConnectedChannelHandler handler = new InboundConnectedChannelHandler();
        handler.setConnectedFrameConsumer(consumer);

        verifyNoInteractions(consumer);
    }

}
