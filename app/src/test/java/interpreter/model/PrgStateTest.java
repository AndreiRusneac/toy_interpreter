package interpreter.model;

import interpreter.exceptions.ADTException;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.NopStmt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrgStateTest {
    @Test
    void ofCreatesEmptyStructuresWithTheProgramOnTheStack() throws ADTException {
        IStmt program = new NopStmt();
        PrgState state = PrgState.of(program);

        assertSame(program, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
        assertTrue(state.getSymTable().toMap().isEmpty());
        assertEquals(0, state.getOut().size());
        assertTrue(state.getFileTable().toMap().isEmpty());
        assertSame(program, state.getOriginalProgram());
    }
}
