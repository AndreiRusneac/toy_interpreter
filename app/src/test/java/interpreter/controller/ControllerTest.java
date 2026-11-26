package interpreter.controller;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ValueExp;
import interpreter.model.exp.VarExp;
import interpreter.model.exp.ReadHeapExp;
import interpreter.model.stmt.AssignStmt;
import interpreter.model.stmt.CompStmt;
import interpreter.model.stmt.IStmt;
import interpreter.model.stmt.PrintStmt;
import interpreter.model.stmt.NewStmt;
import interpreter.model.stmt.VarDeclStmt;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;
import interpreter.repository.Repository;
import interpreter.view.Examples;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    @TempDir
    Path dir;

    private Path log() {
        return dir.resolve("log.txt");
    }

    private Controller controllerFor(PrgState state) {
        return new Controller(new Repository(state, log().toString()), false); // display off
    }

    private PrgState run(IStmt program) throws MyException {
        PrgState state = PrgState.of(program);
        controllerFor(state).allStep();
        return state;
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            n++;
        }
        return n;
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
    void fileExampleReadsBothNumbersAndClosesTheFile() throws Exception {
        Path in = dir.resolve("test.in");
        Files.writeString(in, "15\n50\n");
        PrgState s = run(Examples.fileExample(in.toString()));
        assertEquals(2, s.getOut().size());
        assertEquals(new IntValue(15), s.getOut().get(0));
        assertEquals(new IntValue(50), s.getOut().get(1));
        assertTrue(s.getFileTable().toMap().isEmpty());
    }

    @Test
    void fileExampleWithAnEmptyFilePrintsZeroTwice() throws Exception {
        Path in = dir.resolve("empty.in");
        Files.writeString(in, "");
        PrgState s = run(Examples.fileExample(in.toString()));
        assertEquals("{0, 0}", s.getOut().toString());
        assertTrue(s.getFileTable().toMap().isEmpty());
    }

    @Test
    void fileExampleWithAMissingFileThrows() {
        String missing = dir.resolve("missing.in").toString();
        assertThrows(StatementException.class, () -> run(Examples.fileExample(missing)));
    }

    @Test
    void fileExampleLogShowsTheOpenFile() throws Exception {
        Path in = dir.resolve("test.in");
        Files.writeString(in, "15\n50\n");
        run(Examples.fileExample(in.toString()));
        String log = Files.readString(log());
        assertTrue(log.contains("FileTable:\n" + in + "\n"), "the open file appears in the FileTable section");
        assertTrue(log.contains("varf --> " + new StringValue(in.toString())));
    }

    @Test
    void example5PrintsFive() throws MyException {
        PrgState state = run(Examples.EX5);
        assertTrue(state.getExeStack().isEmpty());
        assertEquals("{5}", state.getOut().toString());
    }


    @Test
    void oneStepAppliesExactlyOneRule() throws MyException {
        PrgState state = PrgState.of(Examples.EX1);
        controllerFor(state).oneStep(state); // S1 only: the CompStmt is split, nothing is declared yet

        assertTrue(state.getSymTable().toMap().isEmpty());
        assertInstanceOf(VarDeclStmt.class, state.getExeStack().pop());
        assertInstanceOf(CompStmt.class, state.getExeStack().pop());
        assertTrue(state.getExeStack().isEmpty());
    }

    @Test
    void oneStepOnEmptyStackThrows() throws MyException {
        PrgState state = PrgState.of(Examples.EX1);
        Controller controller = controllerFor(state);
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


    @Test
    void logFileContainsTheFiveSectionsInOrder() throws Exception {
        run(Examples.EX1);
        String log = Files.readString(log());
        int a = log.indexOf("ExeStack:"), b = log.indexOf("SymTable:"), c = log.indexOf("Out:"),
                d = log.indexOf("FileTable:"), e = log.indexOf("Heap:");
        assertTrue(0 <= a && a < b && b < c && c < d && d < e);
    }

    @Test
    void logHasTheInitialStateAndTwoStatesPerStep() throws Exception {
        run(Examples.EX1);      // 5 steps: the initial state + (before GC, after GC) for every step
        assertEquals(11, count(Files.readString(log()), "ExeStack:"));
    }

    @Test
    void logShowsTheFinalState() throws Exception {
        run(Examples.EX1);
        String log = Files.readString(log());
        String last = log.substring(log.lastIndexOf("ExeStack:"));
        assertEquals("ExeStack:\nSymTable:\nv --> 2\nOut:\n2\nFileTable:\nHeap:\n", last.strip() + "\n");
    }

    @Test
    void logIsAppendedNotOverwritten() throws Exception {
        run(Examples.EX1);
        run(Examples.EX1);
        assertEquals(22, count(Files.readString(log()), "ExeStack:"));
    }

    @Test
    void logIsWrittenUpToTheError() throws Exception {
        IStmt bad = new CompStmt(new VarDeclStmt("a", new IntType()),
                new AssignStmt("a", new ValueExp(new BoolValue(true))));
        assertThrows(StatementException.class, () -> run(bad));
        // the initial state, two states for S1 and two for the declaration; the failing step logs nothing
        assertEquals(5, count(Files.readString(log()), "ExeStack:"));
    }

    // ---------- the garbage collector ----------

    /** Ex9: Ref int v; new(v, 20); Ref Ref int a; new(a, v); new(v, 30); print(rH(rH(a))) */
    private static IStmt ex9() {
        var refInt = new RefType(new IntType());
        return new CompStmt(new VarDeclStmt("v", refInt),
                new CompStmt(new NewStmt("v", new ValueExp(new IntValue(20))),
                        new CompStmt(new VarDeclStmt("a", new RefType(refInt)),
                                new CompStmt(new NewStmt("a", new VarExp("v")),
                                        new CompStmt(new NewStmt("v", new ValueExp(new IntValue(30))),
                                                new PrintStmt(new ReadHeapExp(new ReadHeapExp(new VarExp("a")))))))));
    }

    @Test
    void theUnsafeCollectorMakesExample9FailAtTheLastStep() throws Exception {
        var ex = assertThrows(interpreter.exceptions.ExpressionException.class, () -> run(ex9()));
        assertEquals("rH: address 1 is not in the heap", ex.getMessage());
    }

    @Test
    void theLogShowsTheLocationDisappearingAroundTheLastNew() throws Exception {
        assertThrows(interpreter.exceptions.ExpressionException.class, () -> run(ex9()));
        String log = Files.readString(log());
        // before the collection: all three locations; after it: location 1 is gone
        assertTrue(log.contains("Heap:\n1 --> 20\n2 --> (1, int)\n3 --> 30\n"));
        assertTrue(log.contains("Heap:\n2 --> (1, int)\n3 --> 30\n"));
    }
}
