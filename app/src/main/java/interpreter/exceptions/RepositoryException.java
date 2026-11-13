package interpreter.exceptions;

/** Repository error, e.g. the log file cannot be written. */
public class RepositoryException extends MyException {
    public RepositoryException(String message) {
        super(message);
    }
}
