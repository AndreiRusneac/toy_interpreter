package interpreter.model.stmt;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.exp.ValueExp;
import interpreter.model.exp.VarExp;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrintStmtTest extends StatementTestBase {
    @Test
    void appendsValuesToOutInOrder() throws MyException {
        new PrintStmt(new ValueExp(new IntValue(1))).execute(state);
        new PrintStmt(new ValueExp(new BoolValue(true))).execute(state);
        assertEquals(2, state.getOut().size());
        assertEquals(new IntValue(1), state.getOut().get(0));
        assertEquals(new BoolValue(true), state.getOut().get(1));
    }

    @Test
    void evaluationErrorIsPropagated() {
        IStmt s = new PrintStmt(new VarExp("x"));
        assertThrows(ExpressionException.class, () -> s.execute(state));
        assertEquals(0, state.getOut().size());
    }
}
