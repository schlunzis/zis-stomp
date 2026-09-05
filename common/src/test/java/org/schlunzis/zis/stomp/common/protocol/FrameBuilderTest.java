package org.schlunzis.zis.stomp.common.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrameBuilderTest {

    @Test
    void testBuildFrame() {
        Frame frame = Frame.builder()
                .command(Command.CONNECT)
                .header("accept-version", "1.2")
                .header("host", "example.com")
                .body("Hello, STOMP!")
                .build();

        assertEquals(Command.CONNECT, frame.command());
        assertEquals(1, frame.headers().get("accept-version").size());
        assertEquals("1.2", frame.headers().getFirst("accept-version"));
        assertEquals(1, frame.headers().get("host").size());
        assertEquals("example.com", frame.headers().getFirst("host"));
        assertTrue(frame.body().isPresent());
        assertEquals("Hello, STOMP!", frame.body().get());
    }

    @Test
    void testBuildFrameWithoutBody() {
        Frame frame = Frame.builder()
                .command(Command.DISCONNECT)
                .header("receipt", "77")
                .build();

        assertEquals(Command.DISCONNECT, frame.command());
        assertEquals(1, frame.headers().get("receipt").size());
        assertEquals("77", frame.headers().getFirst("receipt"));
        assertTrue(frame.body().isEmpty());
    }

    @Test
    void testBuildFrameWithoutHeader() {
        Frame frame = Frame.builder()
                .command(Command.CONNECTED)
                .body("Ping")
                .build();

        assertEquals(Command.CONNECTED, frame.command());
        assertEquals(0, frame.headers().size());
        assertTrue(frame.body().isPresent());
        assertEquals("Ping", frame.body().get());
    }

    @Test
    void testBuildFrameWithMultipleHeaders() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("destination", "/queue/test")
                .header("content-type", "text/plain")
                .header("content-length", "13")
                .body("Hello, World!")
                .build();

        assertEquals(Command.SEND, frame.command());
        assertEquals(1, frame.headers().get("destination").size());
        assertEquals("/queue/test", frame.headers().getFirst("destination"));
        assertEquals(1, frame.headers().get("content-type").size());
        assertEquals("text/plain", frame.headers().getFirst("content-type"));
        assertEquals(1, frame.headers().get("content-length").size());
        assertEquals("13", frame.headers().getFirst("content-length"));
        assertTrue(frame.body().isPresent());
        assertEquals("Hello, World!", frame.body().get());
    }

    @Test
    void testBuildFrameWithoutCommand() {
        FrameBuilder builder = Frame.builder()
                .header("some-header", "some-value")
                .body("No command");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void testBuildFrameWithEmptyBody() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("destination", "/queue/empty")
                .body("")
                .build();

        assertEquals(Command.SEND, frame.command());
        assertEquals(1, frame.headers().get("destination").size());
        assertEquals("/queue/empty", frame.headers().getFirst("destination"));
        assertTrue(frame.body().isPresent());
        assertEquals("", frame.body().get());
    }

    @Test
    void testBuildFrameWithNoHeadersNoBody() {
        Frame frame = Frame.builder()
                .command(Command.ACK)
                .build();

        assertEquals(Command.ACK, frame.command());
        assertEquals(0, frame.headers().size());
        assertTrue(frame.body().isEmpty());
    }

    @Test
    void testBuildFrameWithHeadersObject() {
        HeadersImpl headers = new HeadersImpl();
        headers.addFirst("key1", "value1");
        headers.addFirst("key2", "value2");

        Frame frame = Frame.builder()
                .command(Command.SEND)
                .headers(headers)
                .body("Test body")
                .build();

        assertEquals(Command.SEND, frame.command());
        assertEquals(1, frame.headers().get("key1").size());
        assertEquals("value1", frame.headers().getFirst("key1"));
        assertEquals(1, frame.headers().get("key2").size());
        assertEquals("value2", frame.headers().getFirst("key2"));
        assertTrue(frame.body().isPresent());
        assertEquals("Test body", frame.body().get());
    }

    @Test
    void testBuildFrameWithSameHeader() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("destination", "/queue/first")
                .header("destination", "/queue/second")
                .body("Test body")
                .build();

        assertEquals(Command.SEND, frame.command());
        assertEquals(2, frame.headers().get("destination").size());
        assertEquals("/queue/second", frame.headers().get("destination").get(0));
        assertEquals("/queue/first", frame.headers().get("destination").get(1));
        assertTrue(frame.body().isPresent());
        assertEquals("Test body", frame.body().get());
    }

    @Test
    void testBuildFrameWithNullBody() {
        FrameBuilder builder = Frame.builder()
                .command(Command.SEND);

        assertDoesNotThrow(() -> builder.body(null));

        Frame frame = builder.build();
        assertTrue(frame.body().isEmpty());
    }

    @Test
    void testBuildFrameWithNullHeaderKey() {
        FrameBuilder builder = Frame.builder();

        assertThrows(NullPointerException.class, () -> builder.header(null, "value"));
    }

    @Test
    void testBuildFrameWithNullHeaderValue() {
        FrameBuilder builder = Frame.builder();

        assertThrows(NullPointerException.class, () -> builder.header("key", null));
    }

    @Test
    void testBuildFrameWithNullCommand() {
        FrameBuilder builder = Frame.builder();

        assertThrows(NullPointerException.class, () -> builder.command(null));
    }

}
