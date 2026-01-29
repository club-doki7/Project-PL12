package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public final class MetaContext {
    public void unify(Value value1, Value value2) {
        unifyImpl(value1, value2);

        // TODO: solve pendings
    }

    private enum PendingReason {
        NOT_PATTERN("unification is not a pattern unification"),
        NEED_PRUNE("unification is pattern unification only after pruning");

        public final @NotNull String description;

        PendingReason(@NotNull String description) {
            this.description = description;
        }
    }

    private record Pending(Value.Flex flex, Value unifyWith, PendingReason reason) {}

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
