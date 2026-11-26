package interpreter.controller;

import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Works on plain Java collections, not on PrgState, so it can be tested without running a program. */
public final class GarbageCollector {
    private GarbageCollector() {
    }

    /** The addresses that occur in the RefValues of the SymTable. */
    public static Set<Integer> getAddrFromSymTable(Collection<IValue> symTableValues) {
        return symTableValues.stream()
                .filter(v -> v instanceof RefValue)
                .map(v -> ((RefValue) v).address())
                .collect(Collectors.toSet());
    }

    /** UNSAFE: keeps only the heap entries referred to directly from the SymTable. */
    public static Map<Integer, IValue> unsafeGarbageCollector(Set<Integer> symTableAddr,
                                                              Map<Integer, IValue> heap) {
        return heap.entrySet().stream()
                .filter(e -> symTableAddr.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * The heap locations the program can still reach: the roots (the addresses of the SymTable), plus
     * every location whose address is stored in a reachable location, and so on.
     * <p>
     * It always stops: an address is expanded only the first time it is seen (the visited set), the
     * heap is finite, so there are only finitely many first times. This is also why a cycle in the
     * heap could not make it loop forever.
     * <p>
     * Only addresses that are in the heap are returned, so the null address 0 and dangling addresses
     * are left out.
     */
    public static Set<Integer> reachableAddresses(Set<Integer> roots, Map<Integer, IValue> heap) {
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> toVisit = new ArrayDeque<>(roots);       // the work list
        while (!toVisit.isEmpty()) {
            int address = toVisit.pop();
            if (!heap.containsKey(address) || !visited.add(address)) {
                continue;                                        // not a location, or already seen
            }
            if (heap.get(address) instanceof RefValue ref) {
                toVisit.push(ref.address());                     // this location refers to another one
            }
        }
        return visited;
    }

    /** SAFE: keeps every location that is reachable from the SymTable, directly or through the heap. */
    public static Map<Integer, IValue> safeGarbageCollector(Set<Integer> symTableAddr,
                                                            Map<Integer, IValue> heap) {
        Set<Integer> reachable = reachableAddresses(symTableAddr, heap);
        return heap.entrySet().stream()
                .filter(e -> reachable.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
