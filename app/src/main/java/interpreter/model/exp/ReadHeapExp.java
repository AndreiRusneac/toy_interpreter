package interpreter.model.exp;

import interpreter.exceptions.ExpressionException;
import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.adt.IHeap;
import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

/** Rule E6: rH(exp). exp must be a reference; the value is the content of the heap at its address. */
public record ReadHeapExp(IExp exp) implements IExp {
    @Override
    public IValue eval(IDictionary<String, IValue> symTable, IHeap heap) throws MyException {
        if (!(exp.eval(symTable, heap) instanceof RefValue(int address, var ignored))) {
            throw new ExpressionException("rH: " + exp + " is not a reference");
        }
        // checked here, so the error is an ExpressionException with a message about rH
        if (!heap.isDefined(address)) {
            throw new ExpressionException("rH: address " + address + " is not in the heap");
        }
        return heap.get(address);
    }

    @Override
    public String toString() {
        return "rH(" + exp + ")";
    }
}
