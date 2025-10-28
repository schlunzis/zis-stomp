package org.schlunzis.zis.stomp.client;

import org.schlunzis.zis.stomp.client.protocol.Frame;

public sealed interface Message permits Frame {

    Headers headers();

    String body();

}
