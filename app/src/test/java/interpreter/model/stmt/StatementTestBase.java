package interpreter.model.stmt;

import interpreter.exceptions.ADTException;
import interpreter.model.PrgState;
import org.junit.jupiter.api.BeforeEach;

abstract class StatementTestBase {
    protected PrgState state;

    @BeforeEach
    void freshState() throws ADTException {

        state = PrgState.of(new NopStmt());
        state.getExeStack().pop();
    }
}
