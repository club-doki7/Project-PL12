package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

/// ```bnf
/// argument ::= explicit-argument | implicit-argument | named-implicit-argument
/// ```
public sealed interface Argument extends Node {
    boolean isImplicit();

    /// ```bnf
    /// explicit-argument ::= expression
    /// ```
    record Explicit(@NotNull Expr expr) implements Argument {
        @Override
        public boolean isImplicit() {
            return false;
        }

        @Override
        public @NotNull String toString() {
            return expr.toString();
        }
    }

    /// ```bnf
    /// implicit-argument ::= "{" expression "}"
    /// ```
    record Implicit(@NotNull Expr expr,
                    @NotNull Token lbrace,
                    @NotNull Token rbrace) implements Argument {
        @Override
        public boolean isImplicit() {
            return true;
        }

        @Override
        public @NotNull String toString() {
            return "{" + expr + "}";
        }
    }

    /// ```bnf
    /// named-implicit-argument ::= "{" identifier "=" expression "}"
    /// ```
    record NamedImplicit(@NotNull Token name,
                         @NotNull Expr expr,
                         @NotNull Token lbrace,
                         @NotNull Token rbrace,
                         @NotNull Token eq) implements Argument {
        @Override
        public boolean isImplicit() {
            return true;
        }

        @Override
        public @NotNull String toString() {
            return "{" + name.lexeme() + "=" + expr + "}";
        }
    }
}
