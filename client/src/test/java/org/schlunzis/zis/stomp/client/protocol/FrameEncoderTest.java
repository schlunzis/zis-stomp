package org.schlunzis.zis.stomp.client.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class FrameEncoderTest {

    FrameEncoder encoder = new FrameEncoder();

    @Test
    void encodeConnectedFrame() {
        Frame frame = Frame.builder()
                .command(Command.CONNECTED)
                .header("version", "1.2")
                .header("host", "localhost")
                .build();

        String encoded = encoder.encode(frame);

        String expected = """
                CONNECTED
                version:1.2
                host:localhost
                
                \0
                """;
        assertEquals(expected, encoded);
    }

    @Test
    void encodeSendFrameWithBody() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("destination", "/queue/test")
                .body("Hello, STOMP!")
                .build();

        String encoded = encoder.encode(frame);

        String expected = """
                SEND
                destination:/queue/test
                content-length:13
                
                Hello, STOMP!\0
                """;
        assertEquals(expected, encoded);
    }

    @Test
    void encodeFrameWithEscapedHeaders() {
        Frame frame = Frame.builder()
                .command(Command.MESSAGE)
                .header("custom-header", "Line1\nLine2:Value\\Test\rFollowing\r\nEscaped\\n")
                .body("Test Body")
                .build();

        String encoded = encoder.encode(frame);

        String expected = """
                MESSAGE
                custom-header:Line1\\nLine2\\cValue\\\\Test\\rFollowing\\r\\nEscaped\\\\n
                content-length:9
                
                Test Body\0
                """;
        assertEquals(expected, encoded);
    }

    @Test
    void encodeFrameWithWhitespaceHeaders() {
        Frame frame = Frame.builder()
                .command(Command.SEND)
                .header("leading-space", "  value1  ")
                .header("trailing-space", "value2\t")
                .header("both-sides", "\tvalue3  ")
                .body("Whitespace Test")
                .build();

        String encoded = encoder.encode(frame);

        String expected = """
                SEND
                leading-space:  value1 \s
                trailing-space:value2\t
                both-sides:\tvalue3 \s
                content-length:15
                
                Whitespace Test\0
                """;
        assertEquals(expected, encoded);
    }

}
