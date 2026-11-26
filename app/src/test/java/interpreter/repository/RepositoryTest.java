package interpreter.repository;

import interpreter.exceptions.MyException;
import interpreter.exceptions.RepositoryException;
import interpreter.model.PrgState;
import interpreter.model.stmt.NopStmt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @TempDir
    Path dir;

    @Test
    void getCrtPrgReturnsTheInitialState() {
        PrgState state = PrgState.of(new NopStmt());
        assertSame(state, new Repository(state, dir.resolve("log.txt").toString()).getCrtPrg());
    }

    @Test
    void logWritesTheStateInTheLogFormat() throws Exception {
        Path log = dir.resolve("log.txt");
        new Repository(PrgState.of(new NopStmt()), log.toString()).logPrgStateExec();
        assertEquals("ExeStack:\nnop\nSymTable:\nOut:\nFileTable:\nHeap:\n\n", Files.readString(log));
    }

    @Test
    void unwritableLogFileThrowsRepositoryException() {
        var repo = new Repository(PrgState.of(new NopStmt()), dir.toString());
        assertThrows(RepositoryException.class, repo::logPrgStateExec);
    }

    @Test
    void repositoryExceptionIsAnInterpreterException() {
        assertInstanceOf(MyException.class, new RepositoryException("x"));
    }
}
