package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.ValueExp;
import interpreter.model.exp.VarExp;
import interpreter.model.type.BoolType;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Rules S11 (new) and S12 (wH). */
class HeapStmtTest {
    private PrgState state;

    @BeforeEach
    void setUp() throws MyException {
        state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
        new VarDeclStmt("v", new RefType(new IntType())).execute(state);   // Ref int v
    }

    private static ValueExp num(int n) {
        return new ValueExp(new IntValue(n));
    }

    // ---------- new (S11) ----------

    @Test
    void newAllocatesAtAddressOneAndUpdatesTheVariable() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        assertEquals(new RefValue(1, new IntType()), state.getSymTable().lookup("v"));
        assertEquals(new IntValue(20), state.getHeap().get(1));
    }

    @Test
    void everyNewGetsAFreshAddress() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        new NewStmt("v", num(30)).execute(state);
        assertEquals(new RefValue(2, new IntType()), state.getSymTable().lookup("v"));
        assertEquals(new IntValue(20), state.getHeap().get(1), "the old location is not overwritten");
        assertEquals(new IntValue(30), state.getHeap().get(2));
    }

    @Test
    void newCanStoreAReferenceInARefRefVariable() throws MyException {
        new VarDeclStmt("a", new RefType(new RefType(new IntType()))).execute(state);
        new NewStmt("v", num(20)).execute(state);
        new NewStmt("a", new VarExp("v")).execute(state);
        assertEquals(new RefValue(2, new RefType(new IntType())), state.getSymTable().lookup("a"));
        assertEquals(new RefValue(1, new IntType()), state.getHeap().get(2));
    }

    @Test
    void newOfAnUndeclaredVariableThrows() {
        var ex = assertThrows(StatementException.class, () -> new NewStmt("x", num(1)).execute(state));
        assertEquals("new: x is not declared", ex.getMessage());
    }

    @Test
    void newOfANonReferenceVariableThrows() throws MyException {
        new VarDeclStmt("n", new IntType()).execute(state);
        var ex = assertThrows(StatementException.class, () -> new NewStmt("n", num(1)).execute(state));
        assertEquals("new: n is not a reference", ex.getMessage());
    }

    @Test
    void newWithAValueOfTheWrongTypeThrows() {
        var exp = new ValueExp(new BoolValue(true));
        var ex = assertThrows(StatementException.class, () -> new NewStmt("v", exp).execute(state));
        assertEquals("new: true has type bool, expected int", ex.getMessage());
        assertTrue(state.getHeap().getContent().isEmpty(), "nothing is allocated on an error");
    }

    @Test
    void newWithAReferenceOfTheWrongDepthThrows() throws MyException {
        new NewStmt("v", num(20)).execute(state);           // v is a Ref int
        new VarDeclStmt("a", new RefType(new RefType(new IntType()))).execute(state);
        // a needs a Ref int, but 20 is an int
        assertThrows(StatementException.class, () -> new NewStmt("a", num(20)).execute(state));
    }

    // ---------- wH (S12) ----------

    @Test
    void wHChangesTheHeapLocation() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        new WriteHeapStmt("v", num(30)).execute(state);
        assertEquals(new IntValue(30), state.getHeap().get(1));
        assertEquals(new RefValue(1, new IntType()), state.getSymTable().lookup("v"), "v still points to 1");
    }

    @Test
    void wHOfAnUndeclaredVariableThrows() {
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("x", num(1)).execute(state));
        assertEquals("wH: x is not declared", ex.getMessage());
    }

    @Test
    void wHOfANonReferenceVariableThrows() throws MyException {
        new VarDeclStmt("n", new IntType()).execute(state);
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("n", num(1)).execute(state));
        assertEquals("wH: n is not a reference", ex.getMessage());
    }

    @Test
    void wHThroughTheNullReferenceThrows() {
        // v was only declared: it is (0, int), and address 0 is not in the heap
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("v", num(1)).execute(state));
        assertEquals("wH: address 0 is not in the heap", ex.getMessage());
    }

    @Test
    void wHOfAnAddressThatIsNotInTheHeapAnymoreThrows() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        state.getHeap().setContent(java.util.Map.of());      // as if the collector had removed it
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("v", num(1)).execute(state));
        assertEquals("wH: address 1 is not in the heap", ex.getMessage());
    }

    @Test
    void wHWithAValueOfTheWrongTypeThrows() throws MyException {
        new NewStmt("v", num(20)).execute(state);
        var exp = new ValueExp(new BoolValue(true));
        var ex = assertThrows(StatementException.class, () -> new WriteHeapStmt("v", exp).execute(state));
        assertEquals("wH: true has type bool, expected int", ex.getMessage());
        assertEquals(new IntValue(20), state.getHeap().get(1), "the location must stay unchanged");
    }

    @Test
    void toStringIsInfix() {
        assertEquals("new(v, 20)", new NewStmt("v", num(20)).toString());
        assertEquals("wH(v, 30)", new WriteHeapStmt("v", num(30)).toString());
    }

    @Test
    void aBoolReferenceWorksToo() throws MyException {
        new VarDeclStmt("b", new RefType(new BoolType())).execute(state);
        new NewStmt("b", new ValueExp(new BoolValue(true))).execute(state);
        new WriteHeapStmt("b", new ValueExp(new BoolValue(false))).execute(state);
        assertEquals(new BoolValue(false), state.getHeap().get(1));
    }
}
