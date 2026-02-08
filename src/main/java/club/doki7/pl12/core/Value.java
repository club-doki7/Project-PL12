package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

public sealed interface Value {
    record Flex(Term.Meta head, @NotNull ImmSeq<Value> args) implements Value {}

    sealed interface RigidHead permits Term.Bound, Term.Free {}

    record Rigid(RigidHead head, @NotNull ImmSeq<Value> args) implements Value {}

    sealed interface Closure permits Lam, Pi {
        @NotNull ConsRevList<ImmSeq<Value>> localEnv();
        @NotNull Term body();
    }

    record Lam(@NotNull ConsRevList<ImmSeq<Value>> localEnv,
               @NotNull ImmSeq<String> paramNames,
               @NotNull Term body)
        implements Value, Closure
    {}

    record Pi(@NotNull ConsRevList<ImmSeq<Value>> localEnv,
              @NotNull ImmSeq<String> paramNames,
              @NotNull Type paramType,
              @NotNull Term body)
        implements Value, Closure
    {}

    final class Univ implements Value {
        public static final @NotNull Univ UNIV = new Univ();

        private Univ() {}

        @Override
        public @NotNull String toString() {
            return "*";
        }
    }
}
