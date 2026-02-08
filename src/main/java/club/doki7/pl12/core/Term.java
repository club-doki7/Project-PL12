package club.doki7.pl12.core;

import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

public sealed interface Term {
    record Ann(@NotNull Term term, @NotNull Term type) implements Term {}

    record Bound(int index, @NotNull String name) implements Term {}

    record Free(@NotNull Name name) implements Term {}

    record Lam(int paramCount, @NotNull Term body) implements Term {}

    record Univ() implements Term {
        @Override
        public @NotNull String toString() {
            return "*";
        }
    }

    record Pi(@NotNull ImmSeq<@NotNull ParamGroupC> params, @NotNull Term body) implements Term {}

    record App(@NotNull Term func, @NotNull ImmSeq<Term> args) implements Term {}

    record Meta(int id) implements Term {}

    record SolvedMeta(int id, @NotNull Term solution) implements Term {}

    record ParamGroupC(ImmSeq<String> names, Term type) {}
}
