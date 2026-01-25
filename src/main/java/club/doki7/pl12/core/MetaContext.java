package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;

import java.util.ArrayList;
import java.util.HashMap;

public final class MetaContext {
    public void unify(Value value1, Value value2) {
        unifyImpl(value1, value2);

        // TODO: solve pendings
    }

    private record Pending(int metaId, Value unifyWith) {}

    private void unifyImpl(Value value1, Value value2) {
        value1 = force(value1);
        value2 = force(value2);

        // TODO
    }

    private Value force(Value value) {
        while (true) {
            if (!(value instanceof Value.Flex(Term.Meta meta,
                                              ConsRevList<Value> spine,
                                              Term term))) {
                return value;
            }

            Value solution = solutions.get(meta.metaId());
            if (solution == null) {
                return value;
            }
            value = Eval.app(solution, spine, term);
        }
    }

    private final HashMap<Integer, Value> solutions = new HashMap<>();
    private final ArrayList<Pending> pending = new ArrayList<>();
    private int counter = 0;
}
