package org.schlunzis.zis.stomp.client.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.schlunzis.zis.stomp.client.Headers;
import org.schlunzis.zis.stomp.client.ReceiptPolicy;
import org.schlunzis.zis.stomp.client.protocol.Command;
import org.schlunzis.zis.stomp.client.protocol.Frame;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptManagerTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstruction() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        assertDoesNotThrow(() -> new ReceiptManager(receiptTimeout, receiptPolicy));

        assertThrows(NullPointerException.class, () -> new ReceiptManager(null, null));
        assertThrows(NullPointerException.class, () -> new ReceiptManager(receiptTimeout, null));
        assertThrows(NullPointerException.class, () -> new ReceiptManager(null, receiptPolicy));
    }

    @ParameterizedTest
    @EnumSource(value = Command.class, names = {"SEND", "SUBSCRIBE", "UNSUBSCRIBE", "DISCONNECT"})
    void testAttachReceiptIfPolicyEnabledAll(Command command) {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(command);

        Optional<CountDownLatch> latch = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);

        assertTrue(latch.isPresent());
        assertNotNull(latch.get());
        assertEquals(1, latch.get().getCount());
        Headers headers = frameBuilder.build().headers();
        assertTrue(headers.containsKey("receipt"));
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);
        UUID id = assertDoesNotThrow(() -> UUID.fromString(receiptId));
        assertNotEquals(new UUID(0, 0), id);
    }

    @ParameterizedTest
    @EnumSource(value = Command.class, names = {"SEND", "SUBSCRIBE", "UNSUBSCRIBE", "DISCONNECT"})
    void testAttachReceiptIfPolicyEnabledNone(Command command) {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.none();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(command);

        Optional<CountDownLatch> latch = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);

        assertTrue(latch.isEmpty());
        Headers headers = frameBuilder.build().headers();
        assertFalse(headers.containsKey("receipt"));
        String receiptId = headers.getFirst("receipt");
        assertNull(receiptId);
    }

    @Test
    void testAttachReceiptIfPolicyEnabledAllNotForReceipt() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.none();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.CONNECTED); // A command not legible for receipts

        Optional<CountDownLatch> latch = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);

        assertTrue(latch.isEmpty());
        Headers headers = frameBuilder.build().headers();
        assertFalse(headers.containsKey("receipt"));
        String receiptId = headers.getFirst("receipt");
        assertNull(receiptId);
    }

    @Test
    void testHandleReceipt() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        Optional<CountDownLatch> latchOpt = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        assertTrue(latchOpt.isPresent());
        CountDownLatch latch = latchOpt.get();

        Headers headers = frameBuilder.build().headers();
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);

        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                .header("receipt-id", receiptId)
                .build();

        receiptManager.handleReceipt(receiptFrame);

        assertEquals(0, latch.getCount());
    }

    @Test
    void testHandleReceiptWithUnknownReceiptId() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        Optional<CountDownLatch> latchOpt = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        assertTrue(latchOpt.isPresent());
        CountDownLatch latch = latchOpt.get();

        Headers headers = frameBuilder.build().headers();
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);

        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                .header("receipt-id", UUID.randomUUID().toString())
                .build();

        receiptManager.handleReceipt(receiptFrame);

        assertEquals(1, latch.getCount());
    }

    @Test
    void testHandleReceiptWithMissingReceiptIdHeader() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        Optional<CountDownLatch> latchOpt = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        assertTrue(latchOpt.isPresent());
        CountDownLatch latch = latchOpt.get();

        Headers headers = frameBuilder.build().headers();
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);

        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                // Missing "receipt-id" header
                .build();

        receiptManager.handleReceipt(receiptFrame);

        assertEquals(1, latch.getCount());
    }

    @Test
    void testReceiptTimeout() {
        Duration receiptTimeout = Duration.ofMillis(100);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        Duration rt = receiptManager.receiptTimeout();
        assertEquals(receiptTimeout, rt);
    }

    @Test
    void testClear() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        Optional<CountDownLatch> latchOpt = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        assertTrue(latchOpt.isPresent());
        CountDownLatch latch = latchOpt.get();

        Headers headers = frameBuilder.build().headers();
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);

        receiptManager.clear();

        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                .header("receipt-id", receiptId)
                .build();

        receiptManager.handleReceipt(receiptFrame);

        assertEquals(1, latch.getCount());
    }

    @Test
    void testHandleReceiptRemovedAfterFirstCall() {
        Duration receiptTimeout = Duration.ofSeconds(5);
        ReceiptPolicy receiptPolicy = ReceiptPolicy.all();
        ReceiptManager receiptManager = new ReceiptManager(receiptTimeout, receiptPolicy);

        FrameBuilder frameBuilder = Frame.builder()
                .command(Command.SEND);

        Optional<CountDownLatch> latchOpt = receiptManager.attachReceiptIfPolicyEnabled(frameBuilder);
        assertTrue(latchOpt.isPresent());
        CountDownLatch latch = latchOpt.get();

        Headers headers = frameBuilder.build().headers();
        String receiptId = headers.getFirst("receipt");
        assertNotNull(receiptId);

        Frame receiptFrame = Frame.builder()
                .command(Command.RECEIPT)
                .header("receipt-id", receiptId)
                .build();

        receiptManager.handleReceipt(receiptFrame);
        assertEquals(0, latch.getCount());

        assertDoesNotThrow(() -> receiptManager.handleReceipt(receiptFrame));
    }

}
