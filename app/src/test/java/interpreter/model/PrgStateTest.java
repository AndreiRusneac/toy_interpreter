package interpreter.model;

import interpreter.exceptions.ADTException;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.NopStmt;
import interpreter.model.type.IntType;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
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
        assertTrue(state.getHeap().getContent().isEmpty());
        assertSame(program, state.getOriginalProgram());
    }

    @Test
    void everyStateGetsItsOwnHeap() {
        PrgState a = PrgState.of(new NopStmt());
        PrgState b = PrgState.of(new NopStmt());
        a.getHeap().allocate(new IntValue(1));
        assertTrue(b.getHeap().getContent().isEmpty());
    }

    @Test
    void theLogStringHasAHeapSectionAfterTheFileTable() {
        PrgState state = PrgState.of(new NopStmt());
        String log = state.toLogString();
        assertTrue(log.indexOf("FileTable:") < log.indexOf("Heap:"));
        assertTrue(log.endsWith("Heap:\n"));
    }

    @Test
    void theLogStringListsTheHeapSortedByAddress() {
        PrgState state = PrgState.of(new NopStmt());
        for (int i = 1; i <= 12; i++) {                      // hash order differs from numeric order
            state.getHeap().allocate(i == 2 ? new RefValue(1, new IntType()) : new IntValue(i * 10));
        }
        String heapPart = state.toLogString().substring(state.toLogString().indexOf("Heap:"));
        assertTrue(heapPart.startsWith("Heap:\n1 --> 10\n2 --> (1, int)\n3 --> 30\n"));
        assertTrue(heapPart.endsWith("11 --> 110\n12 --> 120\n"));
    }

    @Test
    void theConsoleStringShowsTheHeap() {
        PrgState state = PrgState.of(new NopStmt());
        state.getHeap().allocate(new IntValue(20));
        assertTrue(state.toString().endsWith("Heap:      {1 -> 20}"));
    }
}
