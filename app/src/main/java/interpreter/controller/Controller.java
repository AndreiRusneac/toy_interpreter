package interpreter.controller;

import interpreter.exceptions.MyException;
import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.stmt.IStmt;
import interpreter.repository.IRepository;

public class Controller {
    private final IRepository repo;     // interface type: the implementation can be swapped
    private final boolean displayFlag;  // print the state after every step?

    public Controller(IRepository repo, boolean displayFlag) {
        this.repo = repo;
        this.displayFlag = displayFlag;
    }

    /** Pops the top statement and applies exactly one rule (S1–S6). Empty stack = rule S7 error. */
    public PrgState oneStep(PrgState state) throws MyException {
        var stack = state.getExeStack();
        if (stack.isEmpty()) {
            throw new StatementException("execution stack is empty");
        }
        IStmt crtStmt = stack.pop();
        return crtStmt.execute(state);
    }

    /** Runs the current program until the execution stack is empty. */
    public void allStep() throws MyException {
        PrgState prg = repo.getCrtPrg();
        display(prg);
        while (!prg.getExeStack().isEmpty()) {
            oneStep(prg);
            display(prg);
        }
    }

    private void display(PrgState prg) {
        if (displayFlag) {
            System.out.println(prg);
            System.out.println("----------------------------------------");
        }
    }
}
