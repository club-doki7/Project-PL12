package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;

import java.util.ArrayList;
import java.util.HashMap;

public final class MetaContext {
    private record Pending(int metaId, Value value) {}

    private Value force(Value value) {
        while (true) {
            if (!(value instanceof Value.Flex(Term.Meta meta,
                                              ConsRevList<Value> spine,
                                              _))) {
                return value;
            }

            Value solution = solutions.get(meta.metaId());
            if (solution == null) {
                return value;
            }
            value = Value.app(solution, spine);
        }
    }

    private final HashMap<Integer, Value> solutions = new HashMap<>();
    private final ArrayList<Pending> pending = new ArrayList<>();
    private int counter = 0;
}
