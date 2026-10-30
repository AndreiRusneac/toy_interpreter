package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.exp.ValueExp;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssignStmtTest extends StatementTestBase {
    @Test
    void assignsValueToDeclaredVariable() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        new AssignStmt("v", new ValueExp(new IntValue(2))).execute(state);
        assertEquals(new IntValue(2), state.getSymTable().lookup("v"));
    }

    @Test
    void assignmentToUndeclaredVariableThrows() {
        IStmt s = new AssignStmt("v", new ValueExp(new IntValue(2)));
        var ex = assertThrows(StatementException.class, () -> s.execute(state));
        assertEquals("variable v was not declared", ex.getMessage());
    }

    @Test
    void typeMismatchThrows() throws MyException {
        new VarDeclStmt("v", new IntType()).execute(state);
        IStmt s = new AssignStmt("v", new ValueExp(new BoolValue(true)));
        assertThrows(StatementException.class, () -> s.execute(state));
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"), "value must stay unchanged");
    }
}
