package club.doki7.pl12.elab;

import club.doki7.pl12.syntax.Expr;
import org.jetbrains.annotations.NotNull;

public sealed interface MetaSource {
    record FunParamType(@NotNull Expr.Fun fun, int paramIndex) implements MetaSource {}

    record Hole(@NotNull Expr.Hole hole) implements MetaSource {}
}
