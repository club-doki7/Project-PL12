package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

/// 近表层语法树节点
///
/// {@snippet lang=TEXT :
/// e, ρ, κ ::= e : ρ        (1) 带注解的词项 // @link substring=(1) target=Ann
///           | *            (2) 类型之类型 // @link substring=(2) target=Univ
///           | Πx : ρ . ρ'  (3) 依值函数类型 // @link substring=(3) target=Pi
///           | x            (4) 变量 // @link substring=(4) target=Var
///           | e e'         (5) 应用 // @link substring=(5) target=App
///           | λx . e
///           | λx : τ . e   (6) Lambda 抽象 // @link substring=(6) target=Lam
///           | ?            (7) 洞 // @link substring=(7) target=Hole
/// }
///
/// 简单函数类型 `ρ → ρ'` 可以被看作是依值函数类型 `Π_ : ρ . ρ'` 的语法糖
public sealed interface Expr {
    @NotNull Token location();

    record Ann(@NotNull Expr term, @NotNull Expr annotation) implements Expr {
        @Override
        public @NotNull Token location() {
            return term.location();
        }

        @Override
        public @NotNull String toString() {
            if (term instanceof Var || term instanceof Aster) {
                return term + " : " + annotation;
            } else {
                return "(" + term + ") : " + annotation;
            }
        }
    }

    record Aster(@NotNull Token aster) implements Expr {
        @Override
        public @NotNull Token location() {
            return aster;
        }

        @TestOnly
        public Aster() {
            this(Token.symbol(Token.Kind.ASTER));
        }

        @Override
        public @NotNull String toString() {
            return "*";
        }
    }

    record Pi(@Nullable Token forall,
              boolean implicit,
              @Nullable List<Token> param,
              @NotNull Expr paramType,
              @NotNull Expr bodyType)
            implements Expr
    {
        public Pi(boolean implicit, @NotNull Expr paramType, @NotNull Expr bodyType) {
            this(null, implicit, null, paramType, bodyType);
        }

        @TestOnly
        public Pi(boolean implicit,
                  List<String> params,
                  @NotNull Expr paramType,
                  @NotNull Expr body) {
            Token piToken = Token.symbol(Token.Kind.PI);
            List<Token> paramTokens = params.stream()
                    .map(Token::ident)
                    .toList();
            this(piToken, implicit, paramTokens, paramType, body);
        }

        @Override
        public @NotNull Token location() {
            return (forall != null) ? forall
                    : (param != null && !param.isEmpty()) ? param.getFirst()
                    : paramType.location();
        }

        @Override
        public @NotNull String toString() {
            if (param != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < param.size(); i++) {
                    sb.append(param.get(i).lexeme);
                    if (i != param.size() - 1) {
                        sb.append(", ");
                    }
                }
                String paramsStr = sb.toString();
                return "∀(" + paramsStr + " : " + paramType + ") → " + bodyType;
            } else {
                if (paramType instanceof Pi
                    || paramType instanceof Ann
                    || paramType instanceof Lam) {
                    return "(" + paramType + ") → " + bodyType;
                } else {
                    return paramType + " → " + bodyType;
                }
            }
        }
    }

    record Var(@NotNull Token name) implements Expr {
        @Override
        public @NotNull Token location() {
            return name;
        }

        @TestOnly
        public Var(@NotNull String name) {
            this(Token.ident(name));
        }

        @Override
        public @NotNull String toString() {
            return name.lexeme;
        }
    }

    record App(@NotNull Expr func, @NotNull List<@NotNull Expr> args) implements Expr {
        @TestOnly
        public App(@NotNull Expr func, @NotNull Expr arg) {
            this(func, List.of(arg));
        }

        @Override
        public @NotNull Token location() {
            return func.location();
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            if (func instanceof Lam || func instanceof Pi || func instanceof Ann) {
                sb.append("(").append(func).append(")");
            } else {
                sb.append(func);
            }

            sb.append(" ");

            for (int i = 0; i < args.size(); i++) {
                Expr arg = args.get(i);

                if (arg instanceof App || arg instanceof Lam || arg instanceof Pi || arg instanceof Ann) {
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

    record Lam(@NotNull Token lambda,
               @NotNull List<@NotNull Token> param,
               @NotNull Expr body) implements Expr {
        @Override
        public @NotNull Token location() {
            return lambda;
        }

        @TestOnly
        public Lam(@NotNull String param, @NotNull Expr body) {
            this(Token.symbol(Token.Kind.LAMBDA), List.of(Token.ident(param)), body);
        }

        @TestOnly
        public Lam(@NotNull List<@NotNull String> params, @NotNull Expr body) {
            List<Token> paramTokens = params.stream()
                    .map(Token::ident)
                    .toList();
            this(Token.symbol(Token.Kind.LAMBDA), paramTokens, body);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < param.size(); i++) {
                sb.append("λ").append(param.get(i).lexeme);
                if (i != param.size() - 1) {
                    sb.append(" ");
                }
            }
            String paramsStr = sb.toString();

            if (body instanceof Ann || body instanceof Pi) {
                return paramsStr + ". (" + body + ")";
            } else {
                return paramsStr + ". " + body;
            }
        }
    }

    record Hole(@NotNull Token hole) implements Expr {
        @Override
        public @NotNull Token location() {
            return hole;
        }

        @TestOnly
        public Hole() {
            this(Token.symbol(Token.Kind.QUES));
        }

        @Override
        public @NotNull String toString() {
            return "?";
        }
    }
}
