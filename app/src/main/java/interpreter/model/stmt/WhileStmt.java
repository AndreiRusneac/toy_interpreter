package interpreter.model.stmt;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.exp.IExp;
import interpreter.model.value.BoolValue;

/**
 * Rule S13: while (cond) body.
 * true: push the whole loop back, then push the body on top of it, so the body runs first and the
 * condition is checked again afterwards. false: nothing is pushed, the loop ends.
 */
public record WhileStmt(IExp cond, IStmt body) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws MyException {
        if (!(cond.eval(state.getSymTable(), state.getHeap()) instanceof BoolValue(boolean c))) {
            throw new StatementException("condition " + cond + " is not a boolean");
        }
        if (c) {
            state.getExeStack().push(this);     // under the body, not on top of it
            state.getExeStack().push(body);
        }
        return state;
    }

    @Override
    public String toString() {
        return "(while (" + cond + ") " + body + ")";
    }
}
