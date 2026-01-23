package club.doki7.pl12.core;

import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.Token;
import club.doki7.pl12.util.CommonUtil;
import club.doki7.pl12.util.UV;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public sealed interface Term {
    @NotNull UV<? extends Expr> exprUV();

    default @NotNull Expr expr() {
        return exprUV().v;
    }

    sealed interface Checkable extends Term permits Lam, Inferable {}
    sealed interface Inferable extends Checkable permits Ann, Univ, Pi, Bound, Free, App, Meta {}

    record Lam(@NotNull Checkable body,
               @NotNull UV<String> paramName,
               @NotNull UV<Expr.Lam> exprUV) implements Checkable {}

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
                @Nullable Expr.Hole hole,
                @NotNull Expr intro,
                @Nullable Meta introVar)
            implements Inferable
    {
        @Override
        public @NotNull Expr expr() {
            return Objects.requireNonNullElse(hole, intro);
        }

        @Override
        public @NotNull UV<? extends Expr> exprUV() {
            return new UV<>(expr());
        }

        @Override
        public @NotNull String toString() {
            if (intro instanceof Expr.Var(Token name)) {
                return CommonUtil.subscriptNum("?" + name.lexeme, num);
            }

            return CommonUtil.subscriptNum("?", num);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Meta(int objNum, _, _, _))) return false;
            return this.num == objNum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Meta.class, num);
        }
    }
}
