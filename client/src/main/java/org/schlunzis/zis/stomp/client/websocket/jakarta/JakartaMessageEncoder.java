package org.schlunzis.zis.stomp.client.websocket.jakarta;

import jakarta.websocket.Encoder;
import org.schlunzis.zis.stomp.client.protocol.Message;
import org.schlunzis.zis.stomp.client.protocol.MessageEncoder;

public final class JakartaMessageEncoder implements Encoder.Text<Message> {

    private static final MessageEncoder encoder = new MessageEncoder();

    @Override
    public String encode(Message object) {
        return encoder.encode(object);
    }

}
