package interpreter.model.stmt;

import interpreter.controller.Controller;
import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ArithExp;
import interpreter.model.exp.ArithOp;
import interpreter.model.exp.IExp;
import interpreter.model.exp.RelOp;
import interpreter.model.exp.RelationalExp;
import interpreter.model.exp.ValueExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.IntType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.repository.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WhileStmtTest {
    @TempDir
    Path dir;

    private static IExp num(int n) {
        return new ValueExp(new IntValue(n));
    }

    private static IExp v() {
        return new VarExp("v");
    }

    private PrgState runCountdown(int start) throws MyException {
        IStmt body = new CompStmt(new PrintStmt(v()),
                new AssignStmt("v", new ArithExp(ArithOp.SUB, v(), num(1))));
        IStmt program = new CompStmt(new VarDeclStmt("v", new IntType()),
                new CompStmt(new AssignStmt("v", num(start)),
                        new WhileStmt(new RelationalExp(RelOp.GT, v(), num(0)), body)));
        PrgState state = PrgState.of(program);
        new Controller(new Repository(state, dir.resolve("log.txt").toString()), false).allStep();
        return state;
    }


    private PrgState emptyState() throws MyException {
        PrgState state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
        return state;
    }

    @Test
    void trueConditionPushesTheLoopAndThenTheBody() throws MyException {
        IStmt body = new PrintStmt(num(1));
        var loop = new WhileStmt(new ValueExp(new BoolValue(true)), body);
        PrgState state = emptyState();

        loop.execute(state);

        assertSame(body, state.getExeStack().pop(), "the body runs first");
        assertSame(loop, state.getExeStack().pop(), "then the loop checks its condition again");
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void falseConditionPushesNothing() throws MyException {
        var loop = new WhileStmt(new ValueExp(new BoolValue(false)), new PrintStmt(num(1)));
        PrgState state = emptyState();

        loop.execute(state);

        assertTrue(state.getExeStack().isEmpty());
        assertEquals(0, state.getOut().size());
    }

    @Test
    void aNonBooleanConditionThrows() throws MyException {
        var loop = new WhileStmt(num(1), new NopStmt());
        PrgState state = emptyState();
        var ex = assertThrows(StatementException.class, () -> loop.execute(state));
        assertEquals("condition 1 is not a boolean", ex.getMessage());
        assertTrue(state.getExeStack().isEmpty(), "nothing is pushed on an error");
    }

    @Test
    void anErrorInTheConditionIsPropagated() throws MyException {
        var loop = new WhileStmt(new RelationalExp(RelOp.GT, new VarExp("nope"), num(0)), new NopStmt());
        PrgState state = emptyState();
        assertThrows(MyException.class, () -> loop.execute(state));
    }

    @Test
    void aLoopThatRunsZeroTimes() throws MyException {
        PrgState state = runCountdown(0);
        assertEquals(0, state.getOut().size());
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"));
    }

    @Test
    void aLoopThatRunsOnce() throws MyException {
        PrgState state = runCountdown(1);
        assertEquals("{1}", state.getOut().toString());
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"));
    }

    @Test
    void aLoopThatRunsSeveralTimes() throws MyException {
        PrgState state = runCountdown(4);
        assertEquals("{4, 3, 2, 1}", state.getOut().toString());
        assertEquals(new IntValue(0), state.getSymTable().lookup("v"));
    }

    @Test
    void theStackIsEmptyWhenTheLoopEnds() throws MyException {
        assertTrue(runCountdown(3).getExeStack().isEmpty());
    }

    @Test
    void aLoopCanBeNestedInsideAnIf() throws MyException {
        IStmt loop = new WhileStmt(new RelationalExp(RelOp.LT, v(), num(3)),
                new AssignStmt("v", new ArithExp(ArithOp.ADD, v(), num(1))));
        IStmt program = new CompStmt(new VarDeclStmt("v", new IntType()),
                new IfStmt(new ValueExp(new BoolValue(true)), loop, new NopStmt()));
        PrgState state = PrgState.of(program);
        new Controller(new Repository(state, dir.resolve("log.txt").toString()), false).allStep();
        assertEquals(new IntValue(3), state.getSymTable().lookup("v"));
    }

    @Test
    void toStringIsInfix() {
        var loop = new WhileStmt(new RelationalExp(RelOp.GT, v(), num(0)), new PrintStmt(v()));
        assertEquals("(while ((v > 0)) print(v))", loop.toString());
    }
}
