package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.model.PrgState;

public interface IStmt {
    /** Applies this statement's rule to the program state and returns the state. */
    PrgState execute(PrgState state) throws MyException;
}
