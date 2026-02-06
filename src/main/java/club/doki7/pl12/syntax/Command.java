package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface Command extends Node {
    record Axiom(@NotNull Token name,
                 @NotNull Expr type,
                 @NotNull Token axiom,
                 @NotNull Token dot)
        implements Command
    {
        @Override
        public @NotNull String toString() {
            return "Axiom " + name + " : " + type + ".";
        }
    }

    record Check(@NotNull Expr expr,
                 @NotNull Token check,
                 @NotNull Token dot)
        implements Command
    {
        @Override
        public @NotNull String toString() {
            return "Check " + expr + ".";
        }
    }

    record Definition(@NotNull Token name,
                      @NotNull List<@NotNull ParamGroup> paramGroups,
                      @NotNull Expr type,
                      @NotNull Expr body,
                      @NotNull Token def,
                      @NotNull Token colon,
                      @NotNull Token assign,
                      @NotNull Token dot)
        implements Command
    {
        public Definition {
            assert def.kind() == Token.Kind.KW_DEFINITION
                   || def.kind() == Token.Kind.KW_PROCEDURE;
        }

        public boolean isProcedure() {
            return def.kind() == Token.Kind.KW_PROCEDURE;
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(def.lexeme()).append(' ').append(name);
            for (ParamGroup paramGroup : paramGroups) {
                sb.append(' ').append(paramGroup);
            }
            sb.append(" : ").append(type).append(" := ").append(body).append('.');
            return sb.toString();
        }
    }

    record Notation(@NotNull Token name,
                    @NotNull Operator.Assoc assoc,
                    int prec,
                    @NotNull Expr expr,
                    @NotNull Token notation,
                    @NotNull Token assocToken,
                    @NotNull Token lparen,
                    @NotNull Token rparen,
                    @NotNull Token assign,
                    @NotNull Token dot)
        implements Command
    {
        public Notation {
            assert (assoc == Operator.Assoc.LEFT && assocToken.lexeme().equals("left"))
                   || (assoc == Operator.Assoc.RIGHT && assocToken.lexeme().equals("right"))
                   || (assoc == Operator.Assoc.NONE && assocToken.lexeme().equals("none"));
        }

        @Override
        public @NotNull String toString() {
            return "Notation " + name + " " + assocToken.lexeme() + " " + prec + " " + expr + ".";
        }
    }
}
