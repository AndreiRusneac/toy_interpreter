package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ValueExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.type.StringType;
import interpreter.model.value.IntValue;
import interpreter.model.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Rules S8–S10. Every test gets a fresh temporary folder, so no test depends on project files. */
class FileStmtTest {
    @TempDir
    Path dir;

    private PrgState state;
    private StringValue fileName;     // "<dir>/numbers.in"
    private ValueExp file;            // the expression "<dir>/numbers.in"

    @BeforeEach
    void setUp() throws Exception {
        Path f = dir.resolve("numbers.in");
        Files.writeString(f, "7\n12\n");
        fileName = new StringValue(f.toString());
        file = new ValueExp(fileName);
        state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
        new VarDeclStmt("x", new IntType()).execute(state);
    }

    // ---------- openRFile (S8) ----------

    @Test
    void openAddsTheFileToTheFileTable() throws MyException {
        new OpenRFileStmt(file).execute(state);
        assertTrue(state.getFileTable().isDefined(fileName));
    }

    @Test
    void openWorksWithAStringVariable() throws MyException {
        new VarDeclStmt("f", new StringType()).execute(state);
        new AssignStmt("f", file).execute(state);
        new OpenRFileStmt(new VarExp("f")).execute(state);
        assertTrue(state.getFileTable().isDefined(fileName));
    }

    @Test
    void openWithNonStringExpressionThrows() {
        IStmt s = new OpenRFileStmt(new ValueExp(new IntValue(1)));
        assertThrows(StatementException.class, () -> s.execute(state));
    }

    @Test
    void openingTheSameFileTwiceThrows() throws MyException {
        new OpenRFileStmt(file).execute(state);
        var ex = assertThrows(StatementException.class, () -> new OpenRFileStmt(file).execute(state));
        assertTrue(ex.getMessage().contains("already open"));
    }

    @Test
    void openingAMissingFileThrows() {
        var missing = new ValueExp(new StringValue(dir.resolve("missing.in").toString()));
        var ex = assertThrows(StatementException.class, () -> new OpenRFileStmt(missing).execute(state));
        assertTrue(ex.getMessage().contains("cannot open"));
        assertTrue(state.getFileTable().toMap().isEmpty());
    }

    // ---------- readFile (S9) ----------

    @Test
    void readGivesTheNumbersInOrderThenZeroAtTheEnd() throws MyException {
        new OpenRFileStmt(file).execute(state);
        IStmt read = new ReadFileStmt(file, "x");

        read.execute(state);
        assertEquals(new IntValue(7), state.getSymTable().lookup("x"));
        read.execute(state);
        assertEquals(new IntValue(12), state.getSymTable().lookup("x"));
        read.execute(state);
        assertEquals(new IntValue(0), state.getSymTable().lookup("x"));
        read.execute(state);
        assertEquals(new IntValue(0), state.getSymTable().lookup("x"));
    }

    @Test
    void readIgnoresSurroundingWhitespace() throws Exception {
        Files.writeString(dir.resolve("numbers.in"), "  42  \n");
        new OpenRFileStmt(file).execute(state);
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(42), state.getSymTable().lookup("x"));
    }

    @Test
    void readIntoUndeclaredVariableThrows() throws MyException {
        new OpenRFileStmt(file).execute(state);
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "y").execute(state));
        assertTrue(ex.getMessage().contains("was not declared"));
    }

    @Test
    void readIntoNonIntVariableThrows() throws MyException {
        new VarDeclStmt("b", new BoolType()).execute(state);
        new OpenRFileStmt(file).execute(state);
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "b").execute(state));
        assertTrue(ex.getMessage().contains("is not of type int"));
    }

    @Test
    void readWithNonStringExpressionThrows() {
        IStmt s = new ReadFileStmt(new ValueExp(new IntValue(1)), "x");
        assertThrows(StatementException.class, () -> s.execute(state));
    }

    @Test
    void readFromFileThatIsNotOpenThrows() {
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
        assertTrue(ex.getMessage().contains("is not open"));
    }

    @Test
    void readOfNonIntegerLineThrows() throws Exception {
        Files.writeString(dir.resolve("numbers.in"), "abc\n");
        new OpenRFileStmt(file).execute(state);
        var ex = assertThrows(StatementException.class, () -> new ReadFileStmt(file, "x").execute(state));
        assertTrue(ex.getMessage().contains("is not an integer"));
        assertEquals(new IntValue(0), state.getSymTable().lookup("x"), "x must stay unchanged");
    }

    // ---------- closeRFile (S10) ----------

    @Test
    void closeRemovesTheFileFromTheFileTable() throws MyException {
        new OpenRFileStmt(file).execute(state);
        new CloseRFileStmt(file).execute(state);
        assertFalse(state.getFileTable().isDefined(fileName));
    }

    @Test
    void fileCanBeReopenedAfterClose() throws MyException {
        new OpenRFileStmt(file).execute(state);
        new CloseRFileStmt(file).execute(state);
        new OpenRFileStmt(file).execute(state);
        new ReadFileStmt(file, "x").execute(state);
        assertEquals(new IntValue(7), state.getSymTable().lookup("x"), "reopening starts from the top");
    }

    @Test
    void closingAFileThatIsNotOpenThrows() {
        var ex = assertThrows(StatementException.class, () -> new CloseRFileStmt(file).execute(state));
        assertTrue(ex.getMessage().contains("is not open"));
    }

    @Test
    void closeWithNonStringExpressionThrows() {
        IStmt s = new CloseRFileStmt(new ValueExp(new IntValue(1)));
        assertThrows(StatementException.class, () -> s.execute(state));
    }

    // ---------- toString ----------

    @Test
    void toStringIsInfix() {
        var f = new VarExp("varf");
        assertEquals("openRFile(varf)", new OpenRFileStmt(f).toString());
        assertEquals("readFile(varf, varc)", new ReadFileStmt(f, "varc").toString());
        assertEquals("closeRFile(varf)", new CloseRFileStmt(f).toString());
    }
}
