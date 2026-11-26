package interpreter.model.type;

import interpreter.model.value.IValue;
import interpreter.model.value.RefValue;

/**
 * A reference to a heap location holding a value of type inner. Recursive: inner can be another RefType.
 * Being a record, equals compares the inner types recursively: Ref(Ref(int)) equals Ref(Ref(int))
 * but not Ref(int).
 */
public record RefType(IType inner) implements IType {
    @Override
    public IValue defaultValue() {
        return new RefValue(0, inner);      // address 0 plays the role of null
    }

    @Override
    public String toString() {
        return "Ref(" + inner + ")";
    }
}
