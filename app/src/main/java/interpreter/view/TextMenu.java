package interpreter.view;

import interpreter.controller.Controller;
import interpreter.exceptions.MyException;
import interpreter.model.PrgState;
import interpreter.model.stmt.IStmt;
import interpreter.repository.Repository;

import java.util.List;
import java.util.Scanner;

/** Text menu: select one of the example programs, run it completely. Errors never crash the menu. */
public class TextMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final List<IStmt> programs;
    private IStmt selected;

    public TextMenu(List<IStmt> programs) {
        this.programs = programs;
    }

    public void show() {
        while (true) {
            System.out.println();
            System.out.println("Selected program: " + (selected == null ? "none" : selected));
            System.out.println("1. Select a program");
            System.out.println("2. Run the selected program");
            System.out.println("0. Exit");
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> selectProgram();
                case "2" -> runSelected();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Unknown option: " + option);
            }
        }
    }

    private void selectProgram() {
        for (int i = 0; i < programs.size(); i++) {
            System.out.println((i + 1) + ". " + programs.get(i));
        }
        System.out.print("Program number: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= programs.size()) {
                System.out.println("There is no program with that number.");
                return;
            }
            selected = programs.get(index);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }

    private void runSelected() {
        if (selected == null) {
            System.out.println("Select a program first.");
            return;
        }
        // A fresh state for every run, so the same program can be run again.
        PrgState state = PrgState.of(selected);
        Controller controller = new Controller(new Repository(state, "log.txt"), true);
        try {
            controller.allStep();
            System.out.println("Output: " + state.getOut());
        } catch (MyException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
