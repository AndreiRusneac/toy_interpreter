package interpreter.repository;

import interpreter.exceptions.MyException;
import interpreter.exceptions.RepositoryException;
import interpreter.model.PrgState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/** Stores the program states in a list; for now there is exactly one (one thread). */
public class Repository implements IRepository {
    private final List<PrgState> prgStates = new ArrayList<>();
    private final String logFilePath;

    public Repository(PrgState initialState, String logFilePath) {
        prgStates.add(initialState);
        this.logFilePath = logFilePath;
    }

    @Override
    public PrgState getCrtPrg() {
        return prgStates.getFirst();
    }

    /**
     * Opens the log in append mode (the 'true'), writes one state and closes it again.
     * try-with-resources closes the writer even on an error, so nothing written is lost.
     */
    @Override
    public void logPrgStateExec() throws MyException {
        try (var logFile = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)))) {
            logFile.println(getCrtPrg().toLogString());
        } catch (IOException e) {
            throw new RepositoryException("cannot write the log file " + logFilePath + ": " + e.getMessage());
        }
    }
}
