package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyHeap;
import interpreter.model.type.IntType;
import interpreter.model.type.RefType;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import interpreter.model.value.RefValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Rule E6. */
class ReadHeapExpTest {
    private IDictionary<String, IValue> symTable;
    private IHeap heap;

    @BeforeEach
    void setUp() {
        symTable = new MyDictionary<>();
        heap = new MyHeap();
    }

    @Test
    void readsTheValueAtTheAddress() throws MyException {
        int a = heap.allocate(new IntValue(20));
        symTable.put("v", new RefValue(a, new IntType()));
        assertEquals(new IntValue(20), new ReadHeapExp(new VarExp("v")).eval(symTable, heap));
    }

    @Test
    void canBeNestedToFollowAReferenceToAReference() throws MyException {
        int v = heap.allocate(new IntValue(20));
        int a = heap.allocate(new RefValue(v, new IntType()));
        symTable.put("a", new RefValue(a, new RefType(new IntType())));
        // rH(a) is the reference (1, int); rH(rH(a)) is 20
        assertEquals(new RefValue(1, new IntType()), new ReadHeapExp(new VarExp("a")).eval(symTable, heap));
        assertEquals(new IntValue(20),
                new ReadHeapExp(new ReadHeapExp(new VarExp("a"))).eval(symTable, heap));
    }

    @Test
    void worksInsideAnArithmeticExpression() throws MyException {
        int a = heap.allocate(new IntValue(20));
        symTable.put("v", new RefValue(a, new IntType()));
        IExp e = new ArithExp(ArithOp.ADD, new ReadHeapExp(new VarExp("v")), new ValueExp(new IntValue(5)));
        assertEquals(new IntValue(25), e.eval(symTable, heap));
    }

    @Test
    void anExpressionThatIsNotAReferenceThrows() {
        var e = new ReadHeapExp(new ValueExp(new IntValue(1)));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable, heap));
        assertEquals("rH: 1 is not a reference", ex.getMessage());
    }

    @Test
    void anAddressThatIsNotInTheHeapThrows() {
        symTable.put("v", new RefValue(7, new IntType()));
        var ex = assertThrows(ExpressionException.class,
                () -> new ReadHeapExp(new VarExp("v")).eval(symTable, heap));
        assertEquals("rH: address 7 is not in the heap", ex.getMessage());
    }

    @Test
    void theNullReferenceThrows() {
        symTable.put("v", new RefValue(0, new IntType()));
        var ex = assertThrows(ExpressionException.class,
                () -> new ReadHeapExp(new VarExp("v")).eval(symTable, heap));
        assertEquals("rH: address 0 is not in the heap", ex.getMessage());
    }

    @Test
    void anErrorInTheOperandIsPropagated() {
        assertThrows(ExpressionException.class, () -> new ReadHeapExp(new VarExp("x")).eval(symTable, heap));
    }

    @Test
    void toStringIsInfix() {
        assertEquals("rH(rH(a))", new ReadHeapExp(new ReadHeapExp(new VarExp("a"))).toString());
    }
}
