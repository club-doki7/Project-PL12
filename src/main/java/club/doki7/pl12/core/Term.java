package club.doki7.pl12.core;

import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

public sealed interface Term {
    record Ann(@NotNull Term term, @NotNull Term type) implements Term {}

    record Bound(int index, @NotNull String name) implements Term, Value.RigidHead {}

    record Free(@NotNull Name name) implements Term, Value.RigidHead {}

    record Lam(ImmSeq<String> names, @NotNull Term body) implements Term {}

    final class Univ implements Term {
        public static final @NotNull Univ UNIV = new Univ();

        private Univ() {}

        @Override
        public @NotNull String toString() {
            return "*";
        }
    }

    record Pi(@NotNull ImmSeq<String> names,
              @NotNull Term type,
              @NotNull Term body)
        implements Term {}

    record App(@NotNull Term func, @NotNull ImmSeq<Term> args) implements Term {}

    record Meta(int id) implements Term {}

    record SolvedMeta(int id, @NotNull Term solution) implements Term {}
}
