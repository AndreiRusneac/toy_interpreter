package interpreter.model.stmt;

import interpreter.exceptions.StatementException;
import interpreter.model.PrgState;
import interpreter.model.type.IType;

/** Rule S3: Type Id. Adds Id -> default(Type) to the SymTable. */
public record VarDeclStmt(String name, IType type) implements IStmt {
    @Override
    public PrgState execute(PrgState state) throws StatementException {
        var symTable = state.getSymTable();
        if (symTable.isDefined(name)) {
            throw new StatementException("variable " + name + " is already declared");
        }
        symTable.put(name, type.defaultValue());
        return state;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
