package org.schlunzis.zis.stomp.client.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class MessageDecoder {

    public Message decode(Reader reader) throws DecodingException {
        try (BufferedReader br = new BufferedReader(reader)) {
            final Command command = parseCommand(br);
            final Headers headers = parseHeaders(br);
            final String body = parseBody(br);

            return Message.builder()
                    .command(command)
                    .headers(headers)
                    .body(body)
                    .build();
        } catch (IOException e) {
            throw new DecodingException("", "IO error while decoding STOMP message", e);
        }
    }

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

    private Headers parseHeaders(Reader reader) throws DecodingException, IOException {
        Headers headers = new Headers();
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

    private String parseBody(Reader reader) throws DecodingException, IOException {
        StringBuilder bodyBuilder = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\0') {
                return bodyBuilder.toString();
            }
            bodyBuilder.append((char) ch);
        }
        throw new DecodingException(bodyBuilder.toString(), "STOMP message not properly terminated with null character");
    }

}
