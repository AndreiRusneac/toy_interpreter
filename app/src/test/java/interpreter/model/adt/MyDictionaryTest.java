package interpreter.model.adt;

import interpreter.exceptions.ADTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyDictionaryTest {
    private IDictionary<String, Integer> dict;

    @BeforeEach
    void setUp() {
        dict = new MyDictionary<>();
    }

    @Test
    void putThenLookupReturnsTheValue() throws ADTException {
        dict.put("a", 5);
        assertTrue(dict.isDefined("a"));
        assertEquals(5, dict.lookup("a"));
    }

    @Test
    void missingKeyIsNotDefined() {
        assertFalse(dict.isDefined("a"));
    }

    @Test
    void updateReplacesTheValue() throws ADTException {
        dict.put("a", 5);
        dict.update("a", 7);
        assertEquals(7, dict.lookup("a"));
    }

    @Test
    void lookupOfMissingKeyThrows() {
        assertThrows(ADTException.class, () -> dict.lookup("a"));
    }

    @Test
    void updateOfMissingKeyThrows() {
        assertThrows(ADTException.class, () -> dict.update("a", 1));
    }
}
