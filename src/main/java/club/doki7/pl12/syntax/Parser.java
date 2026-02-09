package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.LexicalException;
import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Parser {
    public static @NotNull Pair<@Nullable Command, @NotNull ParseContext>
    parseCommand(ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p = ctx.nextToken();
        Token tok = p.first();
        ParseContext ctx1 = p.second();

        if (tok.kind() == Token.Kind.EOI) {
            return Pair.of(null, ctx1);
        }

        return switch (tok.kind()) {
            case Token.Kind.KW_AXIOM -> Pair.upcast(parseAxiom(tok, ctx1));
            case Token.Kind.KW_CHECK -> Pair.upcast(parseCheck(tok, ctx1));
            case Token.Kind.KW_DEFINITION,
                 Token.Kind.KW_PROCEDURE -> Pair.upcast(parseDefinition(tok, ctx1));
            case Token.Kind.KW_NOTATION -> Pair.upcast(parseNotation(tok, ctx1));
            default -> throw expectedAnyOfGot(tok, COMMAND_START);
        };
    }

    public static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseExpr(ParseContext ctx) throws ParseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static @NotNull Pair<Command.@NotNull Axiom, @NotNull ParseContext>
    parseAxiom(Token axiomTok, ParseContext ctx) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static @NotNull Pair<Command.@NotNull Check, @NotNull ParseContext>
    parseCheck(Token checkTok, ParseContext ctx) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseExpr(ctx);
        Pair<Token, ParseContext> p2 = expectConsume(p1.second(), Token.Kind.DOT);

        Expr expr = p1.first();
        Token dot = p2.first();
        ParseContext ctx1 = p2.second();

        return Pair.of(new Command.Check(expr, checkTok, dot), ctx1);
    }

    private static @NotNull Pair<Command.@NotNull Definition, @NotNull ParseContext>
    parseDefinition(Token defTok, ParseContext ctx) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static @NotNull Pair<Command.@NotNull Notation, @NotNull ParseContext>
    parseNotation(Token notationTok, ParseContext ctx) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static Pair<Token, ParseContext>
    expectConsume(ParseContext ctx, Token.Kind expected) throws LexicalException, ParseException {
        Pair<Token, ParseContext> p = ctx.nextToken();
        Token got = p.first();
        ParseContext ctx1 = p.second();

        if (got.kind() != expected) {
            throw new ParseException(got.range(), "Expected " + expected + ", got " + got.kind());
        }
        return Pair.of(got, ctx1);
    }

    private static ParseException expectedAnyOfGot(Token got, Token.Kind ...expected) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expected.length; i++) {
            sb.append(expected[i]);
            if (i < expected.length - 2) {
                sb.append(", ");
            } else if (i == expected.length - 2) {
                sb.append(" or ");
            }
        }
        return new ParseException(got.range(), "Expected any of " + sb + ", got " + got.kind());
    }

    private static Token.Kind[] COMMAND_START = new Token.Kind[] {
        Token.Kind.KW_AXIOM,
        Token.Kind.KW_CHECK,
        Token.Kind.KW_DEFINITION,
        Token.Kind.KW_PROCEDURE,
        Token.Kind.KW_NOTATION
    };
}

