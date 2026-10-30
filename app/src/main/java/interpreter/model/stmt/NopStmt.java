package interpreter.model.stmt;

import interpreter.model.PrgState;

/** Rule S6: does nothing. */
public record NopStmt() implements IStmt {
    @Override
    public PrgState execute(PrgState state) {
        return state;
    }

    @Override
    public String toString() {
        return "nop";
    }
}
