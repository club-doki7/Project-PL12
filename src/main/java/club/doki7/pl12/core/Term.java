package club.doki7.pl12.core;

import club.doki7.pl12.syntax.*;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

public sealed interface Term {
    record Ann(@NotNull Term term, @NotNull Term type, @NotNull Expr.Ann source)
        implements Term
    {}

    record Bound(int index, @NotNull Expr source) implements Term {}

    record Free(@NotNull Name name, @NotNull Expr source) implements Term {}

    record Lam(@NotNull ImmSeq<@NotNull Param> params, @NotNull Term body, @NotNull LamSource source)
        implements Term
    {}

    record Univ(@NotNull Expr.Univ source) implements Term {
        @Override
        public @NotNull String toString() {
            return "*";
        }
    }

    record Pi(@NotNull ImmSeq<@NotNull Param> params, @NotNull Term body, @NotNull PiSource source)
        implements Term
    {}

    record App(@NotNull Term func, @NotNull ImmSeq<Term> args)
        implements Term
    {}

    record Meta(int id, @NotNull MetaSource source) implements Term {}

    record SolvedMeta(int id, @NotNull Term solution, @NotNull MetaSource source)
        implements Term
    {}

    sealed interface Param {
        record ParamGroupItem(@NotNull ParamGroup paramGroup,
                              int paramIndex,
                              @NotNull Term type) implements Param {}
        record PartialAppArg(@NotNull Expr.PartialApp partialApp,
                             int argIndex,
                             @NotNull Term type) implements Param {}
        record MetaSolvedLamParam(@NotNull Meta meta,
                                  int paramIndex,
                                  @NotNull Term type) implements Param {}
    }

    sealed interface LamSource {
        record Fun(@NotNull Expr.Fun fun) implements LamSource {}
        record PartialApp(@NotNull Expr.PartialApp partialApp) implements LamSource {}
        record Def(@NotNull Command.Definition def, int paramGroupIndex) implements LamSource {}
        record MetaSolve(@NotNull Meta meta) implements LamSource {}
    }

    sealed interface PiSource {
        record Pi(@NotNull Expr.Pi pi) implements PiSource {}
        record Arrow(@NotNull Expr.Arrow first, @NotNull Expr.Arrow last) implements PiSource {}
        record Def(@NotNull Command.Definition def, int paramGroupIndex) implements PiSource {}
    }

    sealed interface MetaSource {
        record ImplicitArg(@NotNull Param param, @NotNull App app, int argIndex)
            implements MetaSource
        {}
        record FunParamType(@NotNull Expr.Fun fun, int paramIndex) implements MetaSource {}
        record Hole(@NotNull Expr.Hole hole) implements MetaSource {}
    }
}
