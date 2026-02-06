package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

public sealed interface Expr extends Node {
    record Ann(@NotNull Expr term, @NotNull Expr ann, @NotNull Token colon) implements Expr {
        @Override
        public @NotNull String toString() {
            if (term instanceof Var || term instanceof Univ) {
                return term + " : " + ann;
            } else {
                return "(" + term + ") : " + ann;
            }
        }
    }

    record Univ(@NotNull Token aster) implements Expr {
        @TestOnly
        public Univ() {
            this(Token.sym(Token.Kind.UNIV));
        }

        @Override
        public @NotNull String toString() {
            return "𝒰";
        }
    }

    record Pi(@NotNull ParamGroup paramGroup,
              @NotNull Expr body,
              @NotNull Token pi,
              @NotNull Token comma)
            implements Expr
    {
        @Override
        public @NotNull String toString() {
            return "∀" + paramGroup + ", " + body;
        }
    }

    record Var(@NotNull Token name) implements Expr {
        @TestOnly
        public Var(@NotNull String name) {
            this(Token.ident(name));
        }

        @Override
        public @NotNull String toString() {
            return name.lexeme();
        }
    }

    record App(@NotNull Expr func,
               @NotNull List<@NotNull Expr> args,
               boolean infix)
        implements Expr
    {
        @TestOnly
        public App(@NotNull Expr func, @NotNull Expr arg, boolean infix) {
            this(func, List.of(arg), infix);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            if (func instanceof Fun || func instanceof Pi || func instanceof Ann) {
                sb.append("(").append(func).append(")");
            } else {
                sb.append(func);
            }

            sb.append(" ");

            for (int i = 0; i < args.size(); i++) {
                Expr arg = args.get(i);

                if (arg instanceof App
                    || arg instanceof Fun
                    || arg instanceof Pi
                    || arg instanceof Ann) {
                    sb.append("(").append(arg).append(")");
                } else {
                    sb.append(arg);
                }

                if (i != args.size() - 1) {
                    sb.append(" ");
                }
            }

            return sb.toString();
        }
    }

    record Fun(@NotNull List<ParamGroup> paramGroups,
               @NotNull Expr body,
               @NotNull Token fun,
               @NotNull Token arrow)
        implements Expr
    {
        @TestOnly
        public Fun(@NotNull List<ParamGroup> paramGroups, @NotNull Expr body) {
            this(paramGroups, body, Token.sym(Token.Kind.FUN), Token.sym(Token.Kind.ARROW));
        }

        @TestOnly
        public Fun(@NotNull ParamGroup paramGroup, @NotNull Expr body) {
            this(List.of(paramGroup), body);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("fun ");
            for (int i = 0; i < paramGroups.size(); i++) {
                sb.append(paramGroups.get(i));
                if (i != paramGroups.size() - 1) {
                    sb.append(" ");
                }
            }
            sb.append(" => ");

            if (body instanceof Ann || body instanceof Pi) {
                sb.append("(").append(body).append(")");
            } else {
                sb.append(body);
            }
            return sb.toString();
        }
    }

    record Hole(@NotNull Token hole) implements Expr {
        @TestOnly
        public Hole() {
            this(Token.sym(Token.Kind.D_QUES));
        }

        @Override
        public @NotNull String toString() {
            return "?";
        }
    }

    record Paren(@NotNull Expr expr, @NotNull Token lParen, @NotNull Token rParen) implements Expr {
        @TestOnly
        public Paren(@NotNull Expr expr) {
            this(expr, Token.sym(Token.Kind.L_PAREN), Token.sym(Token.Kind.R_PAREN));
        }

        @Override
        public @NotNull String toString() {
            return "(" + expr + ")";
        }
    }
}
