package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.schlunzis.zis.stomp.client.protocol.DecodingException;
import org.schlunzis.zis.stomp.client.protocol.Message;
import org.schlunzis.zis.stomp.client.protocol.MessageDecoder;

import java.io.IOException;
import java.io.Reader;

public final class JakartaMessageDecoder implements Decoder.TextStream<Message> {

    private static final MessageDecoder decoder = new MessageDecoder();

    @Override
    public Message decode(Reader reader) throws DecodeException, IOException {
        try {
            return decoder.decode(reader);
        } catch (DecodingException e) {
            throw new DecodeException(e.getLine(), "Failed to decode STOMP message", e);
        }
    }

}
