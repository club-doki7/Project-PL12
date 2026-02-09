package club.doki7.pl12.elab;

import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.Expr;
import org.jetbrains.annotations.NotNull;

public sealed interface MetaSource {
    record DefParamType(@NotNull Command.Definition def,
                        int paramGroupIndex,
                        int paramIndex)
        implements MetaSource {}

    record FunParamType(@NotNull Expr.Fun fun,
                        int paramGroupIndex,
                        int paramIndex)
        implements MetaSource {}

    record Hole(@NotNull Expr.Hole hole) implements MetaSource {}
}
