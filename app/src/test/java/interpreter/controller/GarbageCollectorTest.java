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

    // ---------- reachableAddresses ----------

    @Test
    void theRootsAreReachable() {
        assertEquals(Set.of(3, 2, 1), GarbageCollector.reachableAddresses(Set.of(3, 2), heap),
                "1 is reachable through 2");
    }

    @Test
    void aLocationReachableOnlyThroughTheHeapIsReachable() {
        assertEquals(Set.of(2, 1), GarbageCollector.reachableAddresses(Set.of(2), heap));
    }

    @Test
    void nothingIsReachableFromNoRoots() {
        assertTrue(GarbageCollector.reachableAddresses(Set.of(), heap).isEmpty());
    }

    @Test
    void theNullAddressAndDanglingAddressesAreNotLocations() {
        assertEquals(Set.of(3), GarbageCollector.reachableAddresses(Set.of(0, 3, 99), heap));
    }

    @Test
    void aLongChainOfReferencesIsFollowedToTheEnd() {
        // Ref Ref Ref int: 4 -> 3 -> 2 -> 1 -> 20
        Map<Integer, IValue> chain = Map.of(
                1, new IntValue(20), 2, new RefValue(1, new IntType()),
                3, new RefValue(2, new RefType(new IntType())), 4, new RefValue(3, new RefType(new RefType(new IntType()))));
        assertEquals(Set.of(4, 3, 2, 1), GarbageCollector.reachableAddresses(Set.of(4), chain));
    }

    @Test
    void aLocationReferredToTwiceIsVisitedOnce() {
        Map<Integer, IValue> shared = Map.of(
                1, new IntValue(1), 2, new RefValue(1, new IntType()), 3, new RefValue(1, new IntType()));
        assertEquals(Set.of(1, 2, 3), GarbageCollector.reachableAddresses(Set.of(2, 3), shared));
    }

    @Test
    void itStopsEvenIfTheHeapHadACycle() {
        // the type system cannot produce a cycle, but the visited set makes the loop stop anyway
        Map<Integer, IValue> cycle = Map.of(
                1, new RefValue(2, new IntType()), 2, new RefValue(1, new IntType()));
        assertEquals(Set.of(1, 2), GarbageCollector.reachableAddresses(Set.of(1), cycle));
    }

    @Test
    void aLocationThatRefersToItselfDoesNotLoop() {
        Map<Integer, IValue> self = Map.of(1, new RefValue(1, new IntType()));
        assertEquals(Set.of(1), GarbageCollector.reachableAddresses(Set.of(1), self));
    }

    // ---------- safeGarbageCollector ----------

    @Test
    void theSafeCollectorKeepsTheLocationsReachableThroughTheHeap() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(symTable);
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(roots, heap);
        assertEquals(Set.of(1, 2, 3), kept.keySet());          // 4 is garbage, 1 is not
    }

    @Test
    void theSafeCollectorKeepsTheValuesUnchanged() {
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(Set.of(2, 3), heap);
        assertEquals(heap.get(1), kept.get(1));
        assertEquals(heap.get(2), kept.get(2));
        assertEquals(heap.get(3), kept.get(3));
    }

    @Test
    void theSafeCollectorRemovesUnreachableLocations() {
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(Set.of(3), heap);
        assertEquals(Set.of(3), kept.keySet());
    }

    @Test
    void theSafeCollectorKeepsNothingIfNothingIsReferenced() {
        assertTrue(GarbageCollector.safeGarbageCollector(Set.of(), heap).isEmpty());
    }

    @Test
    void theSafeCollectorDoesNotChangeTheMapItIsGiven() {
        GarbageCollector.safeGarbageCollector(Set.of(3), heap);
        assertEquals(4, heap.size());
    }

    // ---------- Laboratory 8, exercise 2 ----------
    // SymTable: a --> (3, Ref(int)), c --> (0, int)
    // Heap: 1 --> 7, 2 --> 8, 3 --> (2, int), 4 --> (1, int), 5 --> (4, Ref(int))

    private final List<IValue> exercise2SymTable = List.of(
            new RefValue(3, new RefType(new IntType())), new RefValue(0, new IntType()));
    private final Map<Integer, IValue> exercise2Heap = Map.of(
            1, new IntValue(7), 2, new IntValue(8),
            3, new RefValue(2, new IntType()),
            4, new RefValue(1, new IntType()),
            5, new RefValue(4, new RefType(new IntType())));

    @Test
    void exercise2TheReachableAddressesAreTwoAndThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(exercise2SymTable);
        assertEquals(Set.of(3, 0), roots);
        assertEquals(Set.of(3, 2), GarbageCollector.reachableAddresses(roots, exercise2Heap));
    }

    @Test
    void exercise2TheUnsafeCollectorKeepsOnlyLocationThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(exercise2SymTable);
        assertEquals(Set.of(3), GarbageCollector.unsafeGarbageCollector(roots, exercise2Heap).keySet());
    }

    @Test
    void exercise2TheSafeCollectorKeepsTwoAndThree() {
        Set<Integer> roots = GarbageCollector.getAddrFromSymTable(exercise2SymTable);
        Map<Integer, IValue> kept = GarbageCollector.safeGarbageCollector(roots, exercise2Heap);
        assertEquals(Set.of(2, 3), kept.keySet());
        // 1, 4 and 5 are garbage: 5 refers to 4 and 4 refers to 1, but nothing reachable refers to 5
    }
}
