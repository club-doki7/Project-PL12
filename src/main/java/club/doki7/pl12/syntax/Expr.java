package club.doki7.pl12.syntax;

import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.math.BigInteger;

/// ```bnf
/// expr ::= ann | univ | pi | arrow | var | lit | app | partial-app | fun | hole | paren
/// ```
public sealed interface Expr extends Node {
    /// ```bnf
    /// ann ::= expression ":" expression
    /// ```
    record Ann(@NotNull Expr expr, @NotNull Expr ann, @NotNull Token colon) implements Expr {
        @Override
        public @NotNull String toString() {
            if (expr instanceof Var || expr instanceof Univ) {
                return expr + " : " + ann;
            } else {
                return "(" + expr + ") : " + ann;
            }
        }
    }

    /// ```bnf
    /// univ ::= "𝒰" | "type"
    /// ```
    record Univ(@NotNull Token aster) implements Expr {
        @TestOnly
        public Univ() {
            this(Token.sym(Token.Kind.UNIV));
        }

        @Override
        public @NotNull String toString() {
            return "type";
        }
    }

    /// ```bnf
    /// pi ::= pi-keyword param-group "," expression
    /// pi-keyword ::= "∀" | "Π" | "forall"
    /// ```
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

    record Arrow(@NotNull Expr from,
                 @NotNull Expr to,
                 @NotNull Token arrow)
        implements Expr
    {
        @Override
        public @NotNull String toString() {
            String fromStr =
                (from instanceof App
                 || from instanceof Fun
                 || from instanceof Pi
                 || from instanceof Arrow
                 || from instanceof Ann)
                ? "(" + from + ")"
                : from.toString();
            return fromStr + " -> " + to;
        }
    }

    /// ```bnf
    /// var ::= identifier
    /// ```
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

    /// ```bnf
    /// lit ::= string | nat
    /// ```
    record Lit(@NotNull Token lit) implements Expr {
        public Lit {
            assert (lit instanceof Token.LitString && lit.kind() == Token.Kind.LIT_STRING)
                   || (lit instanceof Token.LitNat && lit.kind() == Token.Kind.LIT_NAT);
        }

        @TestOnly
        public Lit(@NotNull String lit) {
            this(Token.string(lit));
        }

        @TestOnly
        public Lit(long num) {
            this(Token.nat(BigInteger.valueOf(num)));
        }

        @TestOnly
        public Lit(@NotNull BigInteger num) {
            this(Token.nat(num));
        }

        @Override
        public @NotNull String toString() {
            return lit.lexeme();
        }
    }

    /// ```bnf
    /// app ::= expression argument+
    /// ```
    record App(@NotNull Expr func,
               @NotNull ImmSeq<@NotNull Argument> args,
               boolean infix)
        implements Expr
    {
        @TestOnly
        public App(@NotNull Expr func, @NotNull Argument arg, boolean infix) {
            this(func, ImmSeq.of(arg), infix);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            if (func instanceof Fun
                || func instanceof Pi
                || func instanceof Arrow
                || func instanceof Ann) {
                sb.append("(").append(func).append(")");
            } else {
                sb.append(func);
            }

            sb.append(" ");

            for (int i = 0; i < args.size(); i++) {
                Argument arg = args.get(i);

                if (arg instanceof Argument.Explicit(Expr expr)) {
                    if (expr instanceof App
                        || expr instanceof Fun
                        || expr instanceof Pi
                        || expr instanceof Arrow
                        || expr instanceof Ann) {
                        sb.append("(").append(expr).append(")");
                    } else {
                        sb.append(expr);
                    }
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

    /// ```bnf
    /// partial-app ::= "@(" expression argument+ ")"
    /// ```
    record PartialApp(@NotNull Expr func,
                      @NotNull ImmSeq<@NotNull Argument> args,
                      @NotNull Token at,
                      @NotNull Token lparen,
                      @NotNull Token rparen)
        implements Expr
    {}

    /// ```bnf
    /// fun ::= "fun" param-group* "=>" expression
    /// ```
    record Fun(@NotNull ImmSeq<@NotNull ParamGroup> paramGroups,
               @NotNull Expr body,
               @NotNull Token fun,
               @NotNull Token arrow)
        implements Expr
    {
        @TestOnly
        public Fun(@NotNull ImmSeq<ParamGroup> paramGroups, @NotNull Expr body) {
            this(paramGroups, body, Token.sym(Token.Kind.FUN), Token.sym(Token.Kind.ARROW));
        }

        @TestOnly
        public Fun(@NotNull ParamGroup paramGroup, @NotNull Expr body) {
            this(ImmSeq.of(paramGroup), body);
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

    /// ```bnf
    /// hole ::= "?"
    /// ```
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

    /// ```bnf
    /// paren ::= "(" expression ")"
    /// ```
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
