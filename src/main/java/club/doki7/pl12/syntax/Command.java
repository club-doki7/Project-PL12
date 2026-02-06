package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface Command extends Node {
    record Axiom(@NotNull Token name,
                 @NotNull Expr type,
                 @NotNull Token axiom,
                 @NotNull Token dot)
        implements Command
    {}

    record Check(@NotNull Expr expr,
                 @NotNull Token check,
                 @NotNull Token dot)
        implements Command
    {}

    record Definition(@NotNull Token name,
                      @NotNull List<ParameterGroup> paramGroups,
                      @NotNull Expr type,
                      @NotNull Expr body,
                      @NotNull Token def,
                      @NotNull Token colon,
                      @NotNull Token assign,
                      @NotNull Token dot)
        implements Command
    {}
}
