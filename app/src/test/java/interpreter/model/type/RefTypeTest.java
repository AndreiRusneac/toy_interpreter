package interpreter.model.type;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import interpreter.model.stmt.NopStmt;
import interpreter.model.stmt.VarDeclStmt;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefTypeTest {
    @Test
    void defaultValueIsTheNullReferenceOfTheInnerType() {
        assertEquals(new RefValue(0, new IntType()), new RefType(new IntType()).defaultValue());
    }

    @Test
    void aDeclaredRefVariableGetsTheDefaultValue() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        new VarDeclStmt("v", new RefType(new IntType())).execute(state);
        assertEquals(new RefValue(0, new IntType()), state.getSymTable().lookup("v"));
    }

    @Test
    void aDeclaredRefRefVariableGetsTheDefaultValue() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        new VarDeclStmt("a", new RefType(new RefType(new IntType()))).execute(state);
        assertEquals(new RefValue(0, new RefType(new IntType())), state.getSymTable().lookup("a"));
    }

    @Test
    void nestedTypesAreEqualWhenTheirInnerTypesAreEqual() {
        assertEquals(new RefType(new RefType(new IntType())), new RefType(new RefType(new IntType())));
        assertEquals(new RefType(new RefType(new IntType())).hashCode(),
                new RefType(new RefType(new IntType())).hashCode());
    }

    @Test
    void differentNestingDepthsAreDifferent() {
        assertNotEquals(new RefType(new RefType(new IntType())), new RefType(new IntType()));
    }

    @Test
    void sameDepthButDifferentInnerTypesAreDifferent() {
        assertNotEquals(new RefType(new IntType()), new RefType(new BoolType()));
        assertNotEquals(new RefType(new RefType(new IntType())), new RefType(new RefType(new StringType())));
    }

    @Test
    void aRefTypeIsNotTheInnerType() {
        assertNotEquals(new RefType(new IntType()), new IntType());
    }

    @Test
    void toStringNestsTheTypes() {
        assertEquals("Ref(int)", new RefType(new IntType()).toString());
        assertEquals("Ref(Ref(string))", new RefType(new RefType(new StringType())).toString());
    }
}
