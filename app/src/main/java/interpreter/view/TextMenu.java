package interpreter.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A menu built from commands. Adding an option means adding a command; this class never changes.
 * LinkedHashMap keeps the options in the order they were added (a HashMap would shuffle them).
 */
public class TextMenu {
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public void addCommand(Command c) {
        commands.put(c.getKey(), c);
    }

    private void printMenu() {
        for (Command c : commands.values()) {
            System.out.printf("%4s : %s%n", c.getKey(), c.getDescription());
        }
    }

    public void show() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            System.out.print("Input the option: ");
            if (!scanner.hasNextLine()) {           // end of input (e.g. Ctrl+D): stop cleanly
                return;
            }
            Command c = commands.get(scanner.nextLine().strip());
            if (c == null) {
                System.out.println("Invalid option");
                continue;
            }
            c.execute();
        }
    }
}
