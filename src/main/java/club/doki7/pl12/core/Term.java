package club.doki7.pl12.core;

import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.Token;
import club.doki7.pl12.util.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public sealed interface Term {
    @NotNull Expr expr();

    sealed interface Checkable
            extends Term
            permits Defun, Inferable
    {}

    sealed interface Inferable
            extends Checkable
            permits Lam, Ann, Univ, Pi, Bound, Free, App, Meta
    {}

    record Defun(@NotNull Checkable body,
                 @NotNull String paramName,
                 @NotNull Expr.Lam expr)
            implements Checkable
    {
        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("λ").append(paramName);

            Checkable bodyIter = body;
            while (bodyIter instanceof Defun defun) {
                sb.append(" ").append(defun.paramName);
                bodyIter = defun.body;
            }

            sb.append(". ").append(bodyIter);
            return sb.toString();
        }
    }

    record Lam(@NotNull Checkable paramType,
               @NotNull Inferable body,
               @NotNull String paramName,
               @NotNull Expr.Lam expr)
            implements Inferable
    {
        @Override
        public @NotNull String toString() {
            List<String> paramNames = new ArrayList<>();
            paramNames.add(paramName);

            Inferable bodyIter = body;
            while (bodyIter instanceof Lam(Checkable paramType1,
                                           Inferable body1,
                                           String paramName1,
                                           _)) {
                if (!paramType1.equals(paramType)) {
                    break;
                }

                paramNames.add(paramName1);
                bodyIter = body1;
            }

            boolean needParen = paramTypeNeedParen(paramType);

            StringBuilder sb = new StringBuilder();
            sb.append('λ');
            if (needParen) {
                sb.append('(');
            }
            for (String paramName : paramNames) {
                sb.append(paramName).append(' ');
            }
            sb.append(": ").append(paramType);
            if (needParen) {
                sb.append(')');
            }
            sb.append(". ").append(bodyIter);
            return sb.toString();
        }
    }

    record Ann(@NotNull Checkable term, @NotNull Checkable ann, @NotNull Expr.Ann expr)
        implements Inferable
    {
        @Override
        public @NotNull String toString() {
            if (term instanceof Univ
                || term instanceof Free
                || term instanceof Bound
                || term instanceof Meta) {
                return term + " : " + ann;
            } else {
                return "(" + term + ") : " + ann;
            }
        }
    }

    record Univ(@NotNull Expr.Aster expr) implements Inferable {
        @Override
        public @NotNull String toString() {
            return "*";
        }
    }

    record Pi(boolean implicit,
              @NotNull Checkable paramType,
              @NotNull Checkable bodyType,
              @Nullable String paramName,
              @NotNull Expr.Pi expr)
        implements Inferable
    {
        @Override
        public @NotNull String toString() {
            List<Pi> pies = new ArrayList<>();
            pies.add(this);

            Checkable bodyTypeIter = bodyType;
            while (bodyTypeIter instanceof Pi(boolean implicit1,
                                              Checkable paramType1,
                                              Checkable bodyType1,
                                              String paramName1,
                                              _)) {
                if (implicit1 != implicit
                    || !paramType1.equals(paramType)
                    || (paramName1 == null) != (paramName == null)) {
                    break;
                }
                pies.add((Pi) bodyTypeIter);
                bodyTypeIter = bodyType1;
            }

            StringBuilder sb = new StringBuilder();
            if (paramName == null) {
                boolean needParen = paramTypeNeedParen(paramType);
                for (int i = 0; i < pies.size(); i++) {
                    if (implicit) {
                        sb.append('{');
                    } else if (needParen) {
                        sb.append('(');
                    }
                    sb.append(paramType);
                    if (implicit) {
                        sb.append("} -> ");
                    } else if (needParen) {
                        sb.append(") -> ");
                    } else {
                        sb.append(" -> ");
                    }
                }
            } else {
                sb.append(implicit ? "∀{" : "∀(");
                for (Pi pi : pies) {
                    sb.append(pi.paramName).append(' ');
                }
                sb.append(": ").append(paramType);
                sb.append(implicit ? "}, " : "), ");
            }
            sb.append(bodyTypeIter);
            return sb.toString();
        }
    }

    record Bound(Name.Local name, @NotNull Expr.Var expr) implements Inferable {
        @Override
        public @NotNull String toString() {
            return name.toString();
        }
    }

    record Free(@NotNull Name name, @NotNull Expr.Var expr) implements Inferable {
        @Override
        public @NotNull String toString() {
            return name.toString();
        }
    }

    record App(@NotNull Inferable func,
               @NotNull Checkable arg,
               @NotNull Expr.App expr)
        implements Inferable
    {
        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            if (func instanceof Ann) {
                sb.append('(').append(func).append(')');
            } else {
                sb.append(func);
            }

            sb.append(' ');

            if (arg instanceof App
                || arg instanceof Defun
                || arg instanceof Pi
                || arg instanceof Ann) {
                sb.append('(').append(arg).append(')');
            } else {
                sb.append(arg);
            }

            return sb.toString();
        }
    }

    record Meta(int metaId,
                @Nullable Expr.Hole hole,
                @NotNull Expr introCtx,
                @Nullable Expr introVar)
            implements Inferable
    {
        @Override
        public @NotNull Expr expr() {
            if (hole != null) {
                return hole;
            }
            return introCtx;
        }

        @Override
        public @NotNull String toString() {
            if (hole != null) {
                return CommonUtil.subscriptNum("?ẖ", metaId);
            }

            if (introVar instanceof Expr.Var(Token name)) {
                return CommonUtil.subscriptNum("?" + name.lexeme, metaId);
            }

            return CommonUtil.subscriptNum("?α", metaId);
        }
    }

    static boolean paramTypeNeedParen(Term paramType) {
        return paramType instanceof Pi
               || paramType instanceof Lam
               || paramType instanceof Defun
               || paramType instanceof Ann;
    }
}
