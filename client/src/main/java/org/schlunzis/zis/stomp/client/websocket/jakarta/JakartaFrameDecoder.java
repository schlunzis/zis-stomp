package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.schlunzis.zis.stomp.client.protocol.DecodingException;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameDecoder;

import java.io.Reader;

public final class JakartaFrameDecoder implements Decoder.TextStream<Frame> {

    private static final FrameDecoder decoder = new FrameDecoder();

    @Override
    public Frame decode(Reader reader) throws DecodeException {
        try {
            return decoder.decode(reader);
        } catch (DecodingException e) {
            throw new DecodeException(e.getLine(), "Failed to decode STOMP frame", e);
        }
    }

}
