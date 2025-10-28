package org.schlunzis.zis.stomp.client.protocol;

import org.schlunzis.zis.stomp.client.Headers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class FrameDecoder {

    /**
     * Decodes a STOMP frame from the given Reader.
     * The reader must provide a valid STOMP frame according to the STOMP protocol specification.
     * If the frame is invalid, a DecodingException is thrown.
     * <p>
     * The reader will be closed after this method returns or throws an exception.
     *
     * @param reader the Reader to read the STOMP frame from
     * @return the decoded STOMP Frame
     * @throws DecodingException if the frame is invalid or cannot be decoded
     */
    public Frame decode(Reader reader) throws DecodingException {
        try (BufferedReader br = new BufferedReader(reader)) {
            final Command command = parseCommand(br);
            final Headers headers = parseHeaders(br);
            final String body = parseBody(br);

            return Frame.builder()
                    .command(command)
                    .headers(headers)
                    .body(body)
                    .build();
        } catch (IOException e) {
            throw new DecodingException("", "IO error while decoding STOMP frame", e);
        }
    }

    /**
     * Parses the STOMP command from the reader.
     * The line break after the command is consumed.
     *
     * @param reader the Reader to read the command from
     * @return the parsed Command
     * @throws DecodingException if the command is invalid
     * @throws IOException       if an I/O error occurs
     */
    private Command parseCommand(Reader reader) throws DecodingException, IOException {
        StringBuilder commandBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\n') break;
            if (ch != '\r')
                commandBuilder.append((char) ch);
        }
        String commandLine = commandBuilder.toString();
        try {
            return Command.valueOf(commandLine);
        } catch (IllegalArgumentException e) {
            throw new DecodingException(commandLine, "Invalid STOMP command: " + commandLine, e);
        }
    }

    /**
     * Parses the STOMP headers from the reader.
     * If a header value contains escaped characters, they are decoded.
     * If an invalid header is encountered, a DecodingException is thrown.
     * The line break after the headers is consumed.
     * This method stops parsing when an empty line is encountered, indicating the end of headers.
     * The empty line is consumed as well.
     *
     * @param reader the Reader to read the headers from
     * @return the parsed Headers
     * @throws DecodingException if a header is invalid
     * @throws IOException       if an I/O error occurs
     */
    private Headers parseHeaders(Reader reader) throws DecodingException, IOException {
        Headers headers = new HeadersImpl();
        StringBuilder headerBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\n') {
                String line = headerBuilder.toString();
                if (line.isEmpty()) {
                    break;
                }
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    String key = headerParts[0];
                    String value = decodeEscapedHeaderValue(headerParts[1]);
                    headers.add(key, value);
                } else {
                    throw new DecodingException(line, "Invalid STOMP header");
                }
                headerBuilder.setLength(0);
            } else if (ch != '\r') {
                headerBuilder.append((char) ch);
            }
        }
        return headers;
    }

    private String decodeEscapedHeaderValue(String value) throws IOException, DecodingException {
        StringBuilder result = new StringBuilder();
        Reader reader = new StringReader(value);
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\\') {
                int nextCh = reader.read();
                if (nextCh == 'n') {
                    result.append('\n');
                } else if (nextCh == 'c') {
                    result.append(':');
                } else if (nextCh == 'r') {
                    result.append('\r');
                } else if (nextCh == '\\') {
                    result.append('\\');
                } else {
                    throw new DecodingException("" + (char) nextCh, "Invalid escape sequence in STOMP header value");
                }
            } else {
                result.append((char) ch);
            }
        }
        return result.toString();
    }

    /**
     * Parses the STOMP body from the reader.
     * The body is read until a null character ('\0') is encountered.
     * If the null character is not found, a DecodingException is thrown.
     * The null character is not included in the returned body string.
     *
     * @param reader the Reader to read the body from
     * @return the parsed body as a String
     * @throws DecodingException if the body is not properly terminated
     * @throws IOException       if an I/O error occurs
     */
    private String parseBody(Reader reader) throws DecodingException, IOException {
        StringBuilder bodyBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\0') {
                return bodyBuilder.toString();
            }
            bodyBuilder.append((char) ch);
        }
        throw new DecodingException(bodyBuilder.toString(), "STOMP frame not properly terminated with null character");
    }

}
