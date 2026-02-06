package club.doki7.pl12.syntax;

import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ParameterGroup(@NotNull List<@NotNull Token> names,
                             @Nullable Expr type,
                             @Nullable Token colon,
                             @Nullable Pair<@NotNull Token, @NotNull Token> delim) {
    public ParameterGroup {
        assert delim == null || delim.first().kind() == delim.second().kind();
    }

    public boolean implicit() {
        return delim != null && delim.first().kind() == Token.Kind.L_BRACE;
    }
}
