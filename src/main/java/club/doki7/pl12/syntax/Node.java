package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

public sealed interface Node permits Expr {
    @NotNull Token location();
}
