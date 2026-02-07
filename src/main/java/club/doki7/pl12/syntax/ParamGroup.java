package club.doki7.pl12.syntax;

import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// ```bnf
/// param-group ::= "{" identifier+ (":" expr)? "}"
///               | "(" identifier+ (":" expr)? ")"
///               | identifier+ (":" simple-expr)?
///
/// simple-expr ::= to-be-determined
/// ```
public record ParamGroup(@NotNull List<@NotNull Token> names,
                         @Nullable Expr type,
                         @Nullable Token colon,
                         @Nullable Pair<@NotNull Token, @NotNull Token> delim) implements Node {
    public ParamGroup {
        assert delim == null
        || (delim.first().kind() == Token.Kind.L_PAREN && delim.second().kind() == Token.Kind.R_PAREN)
        || (delim.first().kind() == Token.Kind.L_BRACE && delim.second().kind() == Token.Kind.R_BRACE);
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
