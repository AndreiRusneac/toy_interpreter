package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.model.exp.ValueExp;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompStmtTest extends StatementTestBase {
    @Test
    void pushesFirstOnTopOfSecond() throws MyException {
        IStmt first = new PrintStmt(new ValueExp(new IntValue(1)));
        IStmt second = new PrintStmt(new ValueExp(new IntValue(2)));

        new CompStmt(first, second).execute(state);

        assertSame(first, state.getExeStack().pop());
        assertSame(second, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void doesNotTouchSymTableOrOut() throws MyException {
        new CompStmt(new NopStmt(), new NopStmt()).execute(state);
        assertEquals("{}", state.getSymTable().toString());
        assertEquals(0, state.getOut().size());
    }
}
