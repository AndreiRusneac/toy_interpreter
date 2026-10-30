package interpreter.exceptions;

/**
 * Statement execution error: empty execution stack, variable declared twice or not declared,
 * type mismatch in assignment, non-boolean condition.
 */
public class StatementException extends MyException {
    public StatementException(String message) {
        super(message);
    }
}
