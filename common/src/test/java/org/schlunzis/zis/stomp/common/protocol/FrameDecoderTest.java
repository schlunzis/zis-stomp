package org.schlunzis.zis.stomp.common.protocol;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class FrameDecoderTest {

    FrameDecoder decoder = new FrameDecoder();

    private Reader createReader(String frame) {
        return new StringReader(frame);
    }

    @Test
    void decodeValidFrame() throws DecodingException {
        Reader reader = createReader("""
                CONNECT
                accept-version:1.2
                host:example.org
                
                \0
                """
        );

        Frame frame = decoder.decode(reader);
        assertThrows(IOException.class, reader::read); // ensure reader is closed

        assertEquals(Command.CONNECT, frame.command());
        assertEquals(1, frame.headers().get("accept-version").size());
        assertEquals("1.2", frame.headers().get("accept-version").getFirst());
        assertEquals(1, frame.headers().get("host").size());
        assertEquals("example.org", frame.headers().get("host").getFirst());
        assertTrue(frame.body().isEmpty());
    }

    @Test
    void decodeFrameWithBody() throws DecodingException {
        Reader reader = createReader("""
                SEND
                destination:/queue/a
                content-type:text/plain
                
                Hello, World!\0
                """
        );

        Frame frame = decoder.decode(reader);
        assertThrows(IOException.class, reader::read);

        assertEquals(Command.SEND, frame.command());
        assertEquals(1, frame.headers().get("destination").size());
        assertEquals("/queue/a", frame.headers().get("destination").getFirst());
        assertEquals(1, frame.headers().get("content-type").size());
        assertEquals("text/plain", frame.headers().get("content-type").getFirst());
        assertTrue(frame.body().isPresent());
        assertEquals("Hello, World!", frame.body().get());
    }

    @Test
    void decodeFrameWithEscapedHeaders() throws DecodingException {
        Reader reader = createReader("""
                MESSAGE
                destination:/queue/a
                custom-header:Line1\\nLine2\\cColon\\rCarriageReturn\\\\Backslash\\\\nEscapedLineBreakAfterBackslash
                
                Body with special characters: \\n \\c \\r \\\0
                """
        );

        Frame frame = decoder.decode(reader);
        assertThrows(IOException.class, reader::read);

        assertEquals(Command.MESSAGE, frame.command());
        assertEquals(1, frame.headers().get("destination").size());
        assertEquals("/queue/a", frame.headers().get("destination").getFirst());
        assertEquals(1, frame.headers().get("custom-header").size());
        assertEquals("Line1\nLine2:Colon\rCarriageReturn\\Backslash\\nEscapedLineBreakAfterBackslash", frame.headers().get("custom-header").getFirst());
        assertTrue(frame.body().isPresent());
        assertEquals("Body with special characters: \\n \\c \\r \\", frame.body().get());
    }

    @Test
    void decodeFrameWithInvalidCommand() {
        Reader reader = createReader("""
                INVALID_COMMAND
                header1:value1
                
                Body\0
                """
        );

        DecodingException e = assertThrows(DecodingException.class, () -> decoder.decode(reader));
        assertThrows(IOException.class, reader::read);

        assertEquals("INVALID_COMMAND", e.getLine());
        assertEquals("Invalid STOMP command: INVALID_COMMAND", e.getMessage());

    }

    @Test
    void decodeFrameWithInvalidHeader() {
        Reader reader = createReader("""
                CONNECT
                invalid-header
                
                Body\0
                """
        );

        DecodingException e = assertThrows(DecodingException.class, () -> decoder.decode(reader));
        assertThrows(IOException.class, reader::read);

        assertEquals("invalid-header", e.getLine());
        assertEquals("Invalid STOMP header", e.getMessage());

    }

    @Test
    void decodeFrameWithInvalidHeaderValueEscape() {
        Reader reader = createReader("""
                CONNECT
                header1:Value with invalid escape \\t
                
                Body\0
                """
        );

        DecodingException e = assertThrows(DecodingException.class, () -> decoder.decode(reader));
        assertThrows(IOException.class, reader::read);

        assertEquals("t", e.getLine());
        assertEquals("Invalid escape sequence in STOMP header value", e.getMessage());
    }

    @Test
    void decodeFrameWithoutNullTerminator() {
        Reader reader = createReader("""
                CONNECT
                header1:value1
                
                Body
                """
        );

        DecodingException e = assertThrows(DecodingException.class, () -> decoder.decode(reader));
        assertThrows(IOException.class, reader::read);

        assertEquals("Body\n", e.getLine());
        assertEquals("STOMP frame not properly terminated with null character", e.getMessage());
    }

}
