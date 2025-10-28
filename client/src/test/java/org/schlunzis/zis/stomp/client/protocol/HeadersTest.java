package org.schlunzis.zis.stomp.client.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

class HeadersTest {

    @Test
    void testAdd() {
        HeadersImpl headers = new HeadersImpl();
        headers.add("key1", "value1");
        headers.add("key1", "value2");
        headers.add("key2", "value3");

        assertEquals(2, headers.get("key1").size());
        assertEquals("value1", headers.get("key1").get(0));
        assertEquals("value2", headers.get("key1").get(1));
        assertEquals(1, headers.get("key2").size());
        assertEquals("value3", headers.get("key2").get(0));
    }

    @Test
    void testGetFirst() {
        HeadersImpl headers = new HeadersImpl();
        headers.put("key1", List.of("value1", "value2"));
        headers.put("key2", List.of("value3"));

        assertEquals(2, headers.get("key1").size());
        assertEquals("value1", headers.getFirst("key1"));
        assertEquals("value2", headers.get("key1").get(1));
        assertEquals(1, headers.get("key2").size());
        assertEquals("value3", headers.getFirst("key2"));
    }

    @Test
    void testGetFirstNonExistentKey() {
        HeadersImpl headers = new HeadersImpl();
        headers.put("key1", List.of("value1"));

        assertNull(headers.getFirst("nonExistentKey"));
    }

}
