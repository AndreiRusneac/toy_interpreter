package interpreter.view;

import interpreter.controller.Controller;
import interpreter.exceptions.MyException;

/** Runs one example program. Its controller holds one program state, so it can run only once. */
public class RunExampleCommand extends Command {
    private final Controller controller;
    private boolean executed = false;

    public RunExampleCommand(String key, String description, Controller controller) {
        super(key, description);
        this.controller = controller;
    }

    @Override
    public void execute() {
        if (executed) {                         // the state is already used up
            System.out.println("This example has already been executed.");
            return;
        }
        executed = true;
        try {
            controller.allStep();
        } catch (MyException e) {               // errors the controller cannot solve
            System.out.println("Error: " + e.getMessage());
        }
    }
}
