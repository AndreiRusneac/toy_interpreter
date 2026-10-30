package interpreter.model.stmt;

import interpreter.exceptions.ADTException;
import interpreter.model.PrgState;
import interpreter.model.adt.MyDictionary;
import interpreter.model.adt.MyList;
import interpreter.model.adt.MyStack;
import org.junit.jupiter.api.BeforeEach;

/** Gives every statement test a fresh, completely empty program state. */
abstract class StatementTestBase {
    protected PrgState state;

    @BeforeEach
    void freshState() throws ADTException {
        // PrgState pushes the program it is given; pop it so the stack starts empty.
        state = new PrgState(new MyStack<>(), new MyDictionary<>(), new MyList<>(), new NopStmt());
        state.getExeStack().pop();
    }
}
