package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

public sealed interface Argument {
    boolean isImplicit();

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
