package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Parser {
    public @NotNull Pair<@Nullable Command, @NotNull ParseContext>
    parseCommand(ParseContext ctx) throws ParseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseExpr(ParseContext ctx) throws ParseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
