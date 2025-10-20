import org.schlunzis.zis.stomp.processor.StompClientPublisherProcessor;

import javax.annotation.processing.Processor;

module org.schlunzis.stomp.client.annotation.processor {
    requires org.schlunzis.zis.stomp.client;
    requires java.compiler;

    provides Processor with StompClientPublisherProcessor;
}
