package interpreter.model.adt;

import interpreter.exceptions.ADTException;

import java.util.Map;

public interface IDictionary<K, V> {
    boolean isDefined(K key);

    /** Returns the value of an existing key; throws if the key is missing. */
    V lookup(K key) throws ADTException;

    /** Adds a new key (or overwrites an existing one). */
    void put(K key, V value);

    /** Changes the value of an existing key; throws if the key is missing. */
    void update(K key, V value) throws ADTException;

    /** Removes an existing key; throws if the key is missing. */
    void remove(K key) throws ADTException;

    /** A read-only copy of the content. */
    Map<K, V> toMap();
}
