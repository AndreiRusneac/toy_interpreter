package interpreter.controller;

import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The collector works on plain Java collections, so no program has to be run. */
class GarbageCollectorTest {
    // the state of Ex9 after new(v, 30): v -> (3, int), a -> (2, Ref(int))
    private final List<IValue> symTable = List.of(
            new RefValue(3, new IntType()), new RefValue(2, new RefType(new IntType())));
    // 1 is no longer referred to from the SymTable, but location 2 (which a refers to) points to it;
    // 4 is garbage
    private final Map<Integer, IValue> heap = Map.of(
            1, new IntValue(20), 2, new RefValue(1, new IntType()),
            3, new IntValue(30), 4, new IntValue(7));

    @Test
    void getAddrFromSymTableCollectsTheAddressesOfTheReferences() {
        assertEquals(Set.of(3, 2), GarbageCollector.getAddrFromSymTable(symTable));
    }

    @Test
    void getAddrFromSymTableIgnoresValuesThatAreNotReferences() {
        var values = List.<IValue>of(new IntValue(5), new RefValue(7, new IntType()));
        assertEquals(Set.of(7), GarbageCollector.getAddrFromSymTable(values));
    }

    @Test
    void getAddrFromSymTableOfAnEmptySymTableIsEmpty() {
        assertTrue(GarbageCollector.getAddrFromSymTable(List.of()).isEmpty());
    }

    @Test
    void theUnsafeCollectorRemovesUnreachableLocations() {
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(Set.of(2, 3), heap);
        assertFalse(kept.containsKey(4), "4 is garbage");
    }

    @Test
    void theUnsafeCollectorAlsoRemovesALocationThatIsReachableThroughTheHeap() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        Map<Integer, IValue> kept = GarbageCollector.unsafeGarbageCollector(roots, heap);
        // this is the bug: location 1 is reachable through location 2, but it is removed
        assertEquals(Set.of(2, 3), kept.keySet());
    }

    @Test
    void theUnsafeCollectorDoesNotChangeTheMapItIsGiven() {
        GarbageCollector.unsafeGarbageCollector(Set.of(3), heap);
        assertEquals(4, heap.size());
    }
}
