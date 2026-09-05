package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.Encoder;
import org.schlunzis.zis.stomp.common.protocol.Frame;
import org.schlunzis.zis.stomp.common.protocol.FrameEncoder;

public final class JakartaFrameEncoder implements Encoder.Text<Frame> {

    private static final FrameEncoder encoder = new FrameEncoder();

    @Override
    public String encode(Frame object) {
        return encoder.encode(object);
    }

}
