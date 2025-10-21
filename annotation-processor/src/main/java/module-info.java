import org.schlunzis.zis.stomp.processor.StompClientPublisherProcessor;

import javax.annotation.processing.Processor;

/**
 * Module definition for the STOMP client annotation processor.
 * <p>
 * It provides an annotation processor for generating STOMP client publishers at compile time.
 *
 * @since 1.0.0
 */
module org.schlunzis.stomp.client.annotation.processor {
    requires org.schlunzis.zis.stomp.client;
    requires java.compiler;

    provides Processor with StompClientPublisherProcessor;
}
