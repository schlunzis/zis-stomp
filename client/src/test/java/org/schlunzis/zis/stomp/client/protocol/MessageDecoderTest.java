package org.schlunzis.zis.stomp.client.protocol;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageDecoderTest {

    MessageDecoder decoder = new MessageDecoder();

    private Reader createReader(String message) {
        return new StringReader(message);
    }

    @Test
    void decodeValidMessage() throws DecodingException {
        Reader reader = createReader("""
                CONNECT
                accept-version:1.2
                host:example.org
                
                \0
                """
        );

        Message message = decoder.decode(reader);
        assertThrows(IOException.class, reader::read);

        assertEquals(Command.CONNECT, message.command());
        assertEquals(1, message.headers().get("accept-version").size());
        assertEquals("1.2", message.headers().get("accept-version").get(0));
        assertEquals(1, message.headers().get("host").size());
        assertEquals("example.org", message.headers().get("host").get(0));
        assertEquals("", message.body());
    }

    @Test
    void decodeMessageWithBody() throws DecodingException {
        Reader reader = createReader("""
                SEND
                destination:/queue/a
                content-type:text/plain
                
                Hello, World!\0
                """
        );

        Message message = decoder.decode(reader);
        assertThrows(IOException.class, reader::read);

        assertEquals(Command.SEND, message.command());
        assertEquals(1, message.headers().get("destination").size());
        assertEquals("/queue/a", message.headers().get("destination").get(0));
        assertEquals(1, message.headers().get("content-type").size());
        assertEquals("text/plain", message.headers().get("content-type").get(0));
        assertEquals("Hello, World!", message.body());
    }

    @Test
    void decodeMessageWithEscapedHeaders() throws DecodingException {
        Reader reader = createReader("""
                MESSAGE
                destination:/queue/a
                custom-header:Line1\\nLine2\\cColon\\rCarriageReturn\\\\Backslash\\\\nEscapedLineBreakAfterBackslash
                
                Body with special characters: \\n \\c \\r \\\0
                """
        );

        Message message = decoder.decode(reader);
        assertThrows(IOException.class, reader::read);

        assertEquals(Command.MESSAGE, message.command());
        assertEquals(1, message.headers().get("destination").size());
        assertEquals("/queue/a", message.headers().get("destination").get(0));
        assertEquals(1, message.headers().get("custom-header").size());
        assertEquals("Line1\nLine2:Colon\rCarriageReturn\\Backslash\\nEscapedLineBreakAfterBackslash", message.headers().get("custom-header").get(0));
        assertEquals("Body with special characters: \\n \\c \\r \\", message.body());
    }

    @Test
    void decodeWithInvalidCommand() {
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
    void decodeWithInvalidHeader() {
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
    void decodeWithInvalidHeaderValueEscape() {
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
    void decodeWithoutNullTerminator() {
        Reader reader = createReader("""
                CONNECT
                header1:value1
                
                Body
                """
        );

        DecodingException e = assertThrows(DecodingException.class, () -> decoder.decode(reader));
        assertThrows(IOException.class, reader::read);

        assertEquals("Body\n", e.getLine());
        assertEquals("STOMP message not properly terminated with null character", e.getMessage());
    }

}
