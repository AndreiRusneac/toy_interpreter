package interpreter.model.adt;

import interpreter.exceptions.ADTException;

public interface IDictionary<K, V> {
    boolean isDefined(K key);

    /** Returns the value of an existing key; throws if the key is missing. */
    V lookup(K key) throws ADTException;

    /** Adds a new key (or overwrites an existing one). */
    void put(K key, V value);

    /** Changes the value of an existing key; throws if the key is missing. */
    void update(K key, V value) throws ADTException;
}
