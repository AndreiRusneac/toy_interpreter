package interpreter.model.value;

import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefValueTest {
    @Test
    void theTypeIsARefToTheLocationType() {
        assertEquals(new RefType(new IntType()), new RefValue(1, new IntType()).getType());
        assertEquals(new RefType(new RefType(new IntType())),
                new RefValue(10, new RefType(new IntType())).getType());
    }

    @Test
    void valuesWithTheSameAddressAndTypeAreEqual() {
        assertEquals(new RefValue(1, new IntType()), new RefValue(1, new IntType()));
        assertEquals(new RefValue(1, new IntType()).hashCode(), new RefValue(1, new IntType()).hashCode());
    }

    @Test
    void valuesWithDifferentAddressesAreDifferent() {
        assertNotEquals(new RefValue(1, new IntType()), new RefValue(2, new IntType()));
    }

    @Test
    void toStringShowsTheAddressAndTheLocationType() {
        assertEquals("(1, int)", new RefValue(1, new IntType()).toString());
        assertEquals("(2, Ref(int))", new RefValue(2, new RefType(new IntType())).toString());
    }
}
