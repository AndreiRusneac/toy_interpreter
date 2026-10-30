package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.MyDictionary;
import interpreter.model.value.BoolValue;
import interpreter.model.value.IValue;
import interpreter.model.value.IntValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogicExpTest {
    private final IDictionary<String, IValue> symTable = new MyDictionary<>();

    private static IExp bool(boolean b) {
        return new ValueExp(new BoolValue(b));
    }

    private IValue eval(LogicOp op, boolean a, boolean b) throws MyException {
        return new LogicExp(op, bool(a), bool(b)).eval(symTable);
    }

    @Test
    void andIsTrueOnlyWhenBothAreTrue() throws MyException {
        assertEquals(new BoolValue(true), eval(LogicOp.AND, true, true));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, true, false));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, false, true));
        assertEquals(new BoolValue(false), eval(LogicOp.AND, false, false));
    }

    @Test
    void orIsFalseOnlyWhenBothAreFalse() throws MyException {
        assertEquals(new BoolValue(true), eval(LogicOp.OR, true, true));
        assertEquals(new BoolValue(true), eval(LogicOp.OR, true, false));
        assertEquals(new BoolValue(true), eval(LogicOp.OR, false, true));
        assertEquals(new BoolValue(false), eval(LogicOp.OR, false, false));
    }

    @Test
    void nonBooleanFirstOperandThrows() {
        IExp e = new LogicExp(LogicOp.AND, new ValueExp(new IntValue(1)), bool(true));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("first operand is not a boolean", ex.getMessage());
    }

    @Test
    void nonBooleanSecondOperandThrows() {
        IExp e = new LogicExp(LogicOp.OR, bool(true), new ValueExp(new IntValue(1)));
        var ex = assertThrows(ExpressionException.class, () -> e.eval(symTable));
        assertEquals("second operand is not a boolean", ex.getMessage());
    }
}
