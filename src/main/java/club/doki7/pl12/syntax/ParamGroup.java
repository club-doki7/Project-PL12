package club.doki7.pl12.syntax;

import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ParamGroup(@NotNull List<@NotNull Token> names,
                         @Nullable Expr type,
                         @Nullable Token colon,
                         @Nullable Pair<@NotNull Token, @NotNull Token> delim) {
    public ParamGroup {
        assert delim == null || delim.first().kind() == delim.second().kind();
    }

    public boolean isImplicit() {
        return delim != null && delim.first().kind() == Token.Kind.L_BRACE;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        if (delim != null) {
            sb.append(delim.first().lexeme());
        }
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i).lexeme());
            if (i != names.size() - 1) {
                sb.append(' ');
            }
        }
        if (type != null) {
            sb.append(" : ").append(type);
        }
        if (delim != null) {
            sb.append(delim.second().lexeme());
        }
        return sb.toString();
    }
}
