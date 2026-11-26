package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import interpreter.model.type.IntType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MyHeapTest {
    private IHeap heap;

    @BeforeEach
    void setUp() {
        heap = new MyHeap();
    }

    @Test
    void allocateReturnsTheAddressesOneTwoThree() {
        assertEquals(1, heap.allocate(new IntValue(10)));
        assertEquals(2, heap.allocate(new IntValue(20)));
        assertEquals(3, heap.allocate(new IntValue(30)));
    }

    @Test
    void theFirstAddressIsNotZero() {
        assertNotEquals(0, heap.allocate(new IntValue(10)));
        assertFalse(heap.isDefined(0));
    }

    @Test
    void getReturnsTheStoredValue() throws ADTException {
        int a = heap.allocate(new IntValue(10));
        assertEquals(new IntValue(10), heap.get(a));
        assertTrue(heap.isDefined(a));
    }

    @Test
    void getOfAMissingAddressThrows() {
        var ex = assertThrows(ADTException.class, () -> heap.get(5));
        assertEquals("address 5 is not in the heap", ex.getMessage());
    }

    @Test
    void updateReplacesTheValue() throws ADTException {
        int a = heap.allocate(new IntValue(10));
        heap.update(a, new IntValue(99));
        assertEquals(new IntValue(99), heap.get(a));
    }

    @Test
    void updateOfAMissingAddressThrows() {
        assertThrows(ADTException.class, () -> heap.update(5, new IntValue(1)));
        assertFalse(heap.isDefined(5), "a failed update must not create the address");
    }

    @Test
    void getContentIsAReadOnlyCopy() {
        heap.allocate(new IntValue(10));
        Map<Integer, IValue> content = heap.getContent();
        assertEquals(Map.of(1, new IntValue(10)), content);
        assertThrows(UnsupportedOperationException.class, () -> content.put(2, new IntValue(1)));
        heap.allocate(new IntValue(20));
        assertEquals(1, content.size(), "the copy does not follow later changes");
    }

    @Test
    void setContentReplacesTheContent() {
        heap.allocate(new IntValue(10));
        heap.allocate(new IntValue(20));
        heap.setContent(Map.of(2, new IntValue(20)));
        assertFalse(heap.isDefined(1));
        assertTrue(heap.isDefined(2));
    }

    @Test
    void addressesAreNeverReusedAfterSetContent() {
        heap.allocate(new IntValue(10));
        heap.allocate(new IntValue(20));
        heap.setContent(Map.of());
        assertEquals(3, heap.allocate(new IntValue(30)));
    }

    @Test
    void setContentCopiesTheMapItIsGiven() throws ADTException {
        var source = new java.util.HashMap<Integer, IValue>();
        source.put(1, new IntValue(10));
        heap.setContent(source);
        source.put(2, new IntValue(20));
        assertFalse(heap.isDefined(2));
    }

    @Test
    void toStringIsSortedByAddress() {
        heap.allocate(new IntValue(20));
        heap.allocate(new RefValue(1, new IntType()));
        heap.allocate(new IntValue(30));
        assertEquals("{1 -> 20, 2 -> (1, int), 3 -> 30}", heap.toString());
    }
}
