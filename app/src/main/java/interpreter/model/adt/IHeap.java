package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import interpreter.model.value.IValue;

import java.util.Map;

/** Not an ordinary dictionary: the heap itself chooses the address of every new value. */
public interface IHeap {
    /** Stores the value at a new address (1, 2, 3, ...) and returns that address. */
    int allocate(IValue value);

    /** Throws if the address is not in the heap. */
    IValue get(int address) throws ADTException;

    /** Changes the value at an existing address; throws if the address is not in the heap. */
    void update(int address, IValue value) throws ADTException;

    boolean isDefined(int address);

    /** A read-only copy of the content. */
    Map<Integer, IValue> getContent();

    /** Replaces the content (used by the garbage collector). New addresses keep counting up. */
    void setContent(Map<Integer, IValue> content);
}
