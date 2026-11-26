package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyHeap;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelationalExpTest {
    private final IHeap heap = new MyHeap();
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    private static IExp num(int n) {
        return new ValueExp(new IntValue(n));
    }

    private IValue eval(RelOp op, int a, int b) throws MyException {
        return new RelationalExp(op, num(a), num(b)).eval(symTable, heap);
    }

    private static final BoolValue TRUE = new BoolValue(true);
    private static final BoolValue FALSE = new BoolValue(false);

    @Test
    void lessThan() throws MyException {
        assertEquals(TRUE, eval(RelOp.LT, 1, 2));
        assertEquals(FALSE, eval(RelOp.LT, 2, 2));
    }

    @Test
    void lessOrEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.LE, 2, 2));
        assertEquals(FALSE, eval(RelOp.LE, 3, 2));
    }

    @Test
    void equal() throws MyException {
        assertEquals(TRUE, eval(RelOp.EQ, 5, 5));
        assertEquals(FALSE, eval(RelOp.EQ, 5, 6));
    }

    @Test
    void notEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.NE, 5, 6));
        assertEquals(FALSE, eval(RelOp.NE, 5, 5));
    }

    @Test
    void greaterThan() throws MyException {
        assertEquals(TRUE, eval(RelOp.GT, 3, 2));
        assertEquals(FALSE, eval(RelOp.GT, 2, 2));
    }

    @Test
    void greaterOrEqual() throws MyException {
        assertEquals(TRUE, eval(RelOp.GE, 2, 2));
        assertEquals(FALSE, eval(RelOp.GE, 1, 2));
    }

    @Test
    void worksWithVariables() throws MyException {
        symTable.put("a", new IntValue(5));
        symTable.put("b", new IntValue(7));
        IExp e = new RelationalExp(RelOp.LT, new VarExp("a"), new VarExp("b"));
        assertEquals(TRUE, e.eval(symTable, heap));
    }

    @Test
    void nonIntegerFirstOperandThrows() {
        IExp e = new RelationalExp(RelOp.LT, new ValueExp(new BoolValue(true)), num(1));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable, heap));
        assertEquals("first operand is not an integer", ex.getMessage());
    }

    @Test
    void nonIntegerSecondOperandThrows() {
        IExp e = new RelationalExp(RelOp.LT, num(1), new ValueExp(new BoolValue(true)));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable, heap));
        assertEquals("second operand is not an integer", ex.getMessage());
    }

    @Test
    void toStringIsInfix() {
        assertEquals("(a < 7)", new RelationalExp(RelOp.LT, new VarExp("a"), num(7)).toString());
    }
}
