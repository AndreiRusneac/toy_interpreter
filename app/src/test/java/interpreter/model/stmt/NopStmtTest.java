package interpreter.model.stmt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NopStmtTest extends StatementTestBase {
    @Test
    void leavesTheStateUnchanged() {
        assertSame(state, new NopStmt().execute(state));
        assertTrue(state.getExeStack().isEmpty());
        assertEquals("{}", state.getSymTable().toString());
        assertEquals(0, state.getOut().size());
    }
}
