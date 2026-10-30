package interpreter.controller;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import interpreter.model.exp.ValueExp;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.CompStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.VarDeclStmt;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.repository.Repository;
import interpreter.view.Examples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    private static PrgState newState(IStmt program) {
        return new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), program);
    }

    private static PrgState run(IStmt program) throws MyException {
        PrgState state = newState(program);
        new Controller(new Repository(state), false).allStep(); // display off
        return state;
    }

    @Test
    void example1PrintsTwo() throws MyException {
        PrgState state = run(Examples.EX1);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals("{2}", state.getOut().toString());
    }

    @Test
    void example2PrintsEighteen() throws MyException {
        PrgState state = run(Examples.EX2);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals(1, state.getOut().size());
        assertEquals(new IntValue(18), state.getOut().get(0));
    }

    @Test
    void example3PrintsTwo() throws MyException {
        PrgState state = run(Examples.EX3);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals("{2}", state.getOut().toString());
        assertEquals(new BoolValue(true), state.getSymTable().lookup("a"));
    }

    @Test
    void oneStepAppliesExactlyOneRule() throws MyException {
        PrgState state = newState(Examples.EX1);
        Controller controller = new Controller(new Repository(state), false);

        controller.oneStep(state); // S1 only: the CompStmt is split, nothing is declared yet

        assertEquals("{}", state.getSymTable().toString());
        assertInstanceOf(VarDeclStmt.class, state.getExeStack().pop());
        assertInstanceOf(CompStmt.class, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void oneStepOnEmptyStackThrows() throws MyException {
        PrgState state = newState(Examples.EX1);
        Controller controller = new Controller(new Repository(state), false);
        controller.allStep();

        var ex = assertThrows(StatementException.class, () -> controller.oneStep(state));
        assertEquals("execution stack is empty", ex.getMessage());
    }

    @Test
    void runtimeErrorStopsExecution() {
        // int a; a = true  -> type mismatch
        IStmt bad = new CompStmt(new VarDeclStmt("a", new IntType()),
                new AssignStmt("a", new ValueExp(new BoolValue(true))));
        assertThrows(StatementException.class, () -> run(bad));
    }
}
