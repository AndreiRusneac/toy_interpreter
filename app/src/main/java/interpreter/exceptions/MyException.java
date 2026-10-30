package interpreter.exceptions;

/** Root of the interpreter's exception hierarchy. Checked, so every caller must handle it. */
public class MyException extends Exception {
    public MyException(String message) {
        super(message);
    }
}
