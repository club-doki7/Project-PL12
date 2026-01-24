package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public sealed interface Value {
    @NotNull Term term();

    record Flex(@NotNull Term.Meta meta,
                @NotNull ConsRevList<@NotNull Value> spine,
                @NotNull Term term)
            implements Value
    {}

    record Rigid(@NotNull Name name,
                 @NotNull ConsRevList<@NotNull Value> spine,
                 @NotNull Term term)
            implements Value
    {}

    record Lam(@NotNull Name name,
               @NotNull Env env,
               @NotNull ConsRevList<@NotNull Value> localEnv,
               @NotNull Term body,
               @NotNull Term term)
            implements Value
    {
        public Lam {
            assert term instanceof Term.Lam || term instanceof Term.LamInf;
        }
    }

    record Pi(@NotNull Name name,
              @NotNull Env env,
              @NotNull Value paramType,
              @NotNull ConsRevList<@NotNull Value> localEnv,
              @NotNull Term returnType,
              @NotNull Term.Pi term)
            implements Value
    {}

    record Univ(@NotNull Term.Univ term) implements Value {}

    static Value app(Value head, Value arg) {
        switch (head) {
            case Flex(Term.Meta meta, ConsRevList<Value> headSpine, Term term) -> {
                ConsRevList<Value> newSpine = new ConsRevList.Cons<>(headSpine, arg);
                return new Flex(meta, newSpine, term);
            }
            case Rigid(Name name, ConsRevList<Value> headSpine, Term term) -> {
                ConsRevList<Value> newSpine = new ConsRevList.Cons<>(headSpine, arg);
                return new Rigid(name, newSpine, term);
            }
            case Lam(Name name, Env env, ConsRevList<Value> localEnv, Term body, Term term) -> {
                throw new UnsupportedOperationException("Not implemented yet");
            }
            default -> throw new IllegalStateException("Cannot apply to head: " + head);
        }
    }

    static Value app(Value head, ConsRevList<Value> spine) {
        List<Value> values = new ArrayList<>();
        while (spine instanceof ConsRevList.Cons<Value>(ConsRevList<Value> init, Value last)) {
            values.add(last);
            spine = init;
        }

        Value result = head;
        for (int i = values.size() - 1; i >= 0; i--) {
            result = app(result, values.get(i));
        }
        return result;
    }
}
