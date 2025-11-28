package org.schlunzis.zis.stomp.client.internal.interaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractInteractionContextTest {

    AbstractInteractionContext<EmptyInteractionContext<String>> context;

    @BeforeEach
    void setUp() {
        context = new EmptyInteractionContext<>();
    }

    @Test
    void testHeadersOne() {
        context.header("key1", "value1");

        assertEquals("value1", context.headers().getFirst("key1"));
    }

    @Test
    void testHeadersMultiple() {
        context.header("key1", "value1")
                .header("key2", "value2");

        assertEquals("value1", context.headers().getFirst("key1"));
        assertEquals("value2", context.headers().getFirst("key2"));
    }

    @Test
    void testHeadersOverwrite() {
        context.header("key1", "value1")
                .header("key1", "value2");

        assertEquals("value2", context.headers().getFirst("key1"));
        assertEquals("value1", context.headers().get("key1").get(1));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testHeadersNull() {
        assertThrows(NullPointerException.class, () -> context.header(null, "value"));
        assertThrows(NullPointerException.class, () -> context.header("key", null));
        assertThrows(NullPointerException.class, () -> context.header(null, null));
    }

}
