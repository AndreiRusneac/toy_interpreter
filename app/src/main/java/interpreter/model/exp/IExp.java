package interpreter.model.exp;

import interpreter.exceptions.MyException;
import interpreter.model.adt.IDictionary;
import interpreter.model.value.IValue;

public interface IExp {
    /** Evaluates the expression against the SymTable. Never changes the program state. */
    IValue eval(IDictionary<String, IValue> symTable) throws MyException;
}
