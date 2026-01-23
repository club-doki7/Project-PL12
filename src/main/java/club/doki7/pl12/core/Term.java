package club.doki7.pl12.core;

import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.Token;
import club.doki7.pl12.util.CommonUtil;
import club.doki7.pl12.util.UV;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Term {
    @NotNull UV<? extends Expr> exprUV();

    default @NotNull Expr expr() {
        return exprUV().e;
    }

    sealed interface Checkable
            extends Term
            permits Lam, Inferable {
    }

    sealed interface Inferable
            extends Checkable
            permits LamInf, Ann, Univ, Pi, Bound, Free, App, Meta {
    }

    record Lam(@NotNull Checkable body,
               @NotNull UV<String> paramName,
               @NotNull UV<Expr.Lam> exprUV)
            implements Checkable
    {}

    record LamInf(@NotNull Checkable paramType,
                  @NotNull Inferable body,
                  @NotNull UV<String> paramName,
                  @NotNull UV<Expr.Lam> exprUV)
            implements Inferable
    {}

    record Ann(@NotNull Checkable term, @NotNull Checkable ann, @NotNull UV<Expr.Ann> exprUV)
        implements Inferable
    {}

    record Univ(@NotNull UV<Expr.Aster> exprUV) implements Inferable {}

    record Pi(boolean implicit,
              @NotNull Checkable paramType,
              @NotNull Checkable bodyType,
              @Nullable UV<String> paramName,
              @NotNull UV<Expr.Pi> exprUV)
        implements Inferable
    {}

    record Bound(@NotNull Name name, @NotNull UV<Expr.Var> exprUV) implements Inferable {}

    record Free(@NotNull Name name, @NotNull UV<Expr.Var> exprUV) implements Inferable {}

    record App(@NotNull Inferable func,
               @NotNull List<@NotNull Checkable> args,
               @NotNull UV<Expr.App> exprUV)
        implements Inferable
    {}

    record Meta(int num,
                @Nullable UV<Expr.Hole> hole,
                @NotNull UV<Expr> intro,
                @Nullable UV<Meta> introVar)
            implements Inferable
    {
        @Override
        public @NotNull Expr expr() {
            if (hole != null) {
                return hole.e;
            }
            return intro.e;
        }

        @Override
        public @NotNull UV<? extends Expr> exprUV() {
            return new UV<>(expr());
        }

        @Override
        public @NotNull String toString() {
            if (intro.e instanceof Expr.Var(Token name)) {
                return CommonUtil.subscriptNum("?" + name.lexeme, num);
            }

            return CommonUtil.subscriptNum("?", num);
        }
    }
}
