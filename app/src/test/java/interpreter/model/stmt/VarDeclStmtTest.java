package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VarDeclStmtTest extends StatementTestBase {
    @Test
    void intVariableDefaultsToZero() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"));
    }

    @Test
    void boolVariableDefaultsToFalse() throws MyException {
        new VarDeclStmt("b", new BoolType()).execute(state);
        assertEquals(new BoolValue(false), state.getSymTable().lookup("b"));
    }

    @Test
    void redeclaredVariableThrows() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        IStmt again = new VarDeclStmt("v", new BoolType());
        var ex = assertThrows(StatementException.class, () -> again.execute(state));
        assertEquals("variable v is already declared", ex.getMessage());
    }
}
