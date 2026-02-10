package club.doki7.pl12.core;

import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Value {
    record Flex(Term.Meta head, @NotNull ImmSeq<Value> args) implements Value {}

    sealed interface RigidHead permits Term.Bound, Term.Free, Lam {}

    record Rigid(RigidHead head, @NotNull ImmSeq<Value> args) implements Value {}

    record Lam(@NotNull SnocList<ImmSeq<Value>> localEnv,
               @NotNull ImmSeq<String> paramNames,
               @NotNull Term body)
        implements RigidHead
    {}

    record Pi(@NotNull SnocList<ImmSeq<Value>> localEnv,
              @Nullable String paramName,
              @NotNull Type paramType,
              @NotNull Term body)
        implements Value
    {}

    final class Univ implements Value {
        private static final @NotNull Univ UNIV = new Univ();

        private Univ() {}

        @Override
        public @NotNull String toString() {
            return "type";
        }
    }

    @NotNull Univ UNIV = Univ.UNIV;
}
