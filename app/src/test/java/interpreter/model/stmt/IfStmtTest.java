package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.exp.ValueExp;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IfStmtTest extends StatementTestBase {
    private final IStmt thenS = new PrintStmt(new ValueExp(new IntValue(1)));
    private final IStmt elseS = new PrintStmt(new ValueExp(new IntValue(2)));

    @Test
    void trueConditionPushesThenBranch() throws MyException {
        new IfStmt(new ValueExp(new BoolValue(true)), thenS, elseS).execute(state);
        assertSame(thenS, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void falseConditionPushesElseBranch() throws MyException {
        new IfStmt(new ValueExp(new BoolValue(false)), thenS, elseS).execute(state);
        assertSame(elseS, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void nonBooleanConditionThrows() {
        IStmt s = new IfStmt(new ValueExp(new IntValue(1)), thenS, elseS);
        assertThrows(StatementException.class, () -> s.execute(state));
    }
}
