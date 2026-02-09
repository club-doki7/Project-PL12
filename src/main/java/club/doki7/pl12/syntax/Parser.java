package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
    parseAxiom(Token axiomTok, ParseContext ctx) throws ParseException {
        Pair<ImmSeq<Token>, ParseContext> p1 = parseIdentList(ctx);
        Pair<Token, ParseContext> p2 = expectConsume(p1.second(), Token.Kind.COLON);
        Pair<Expr, ParseContext> p3 = parseExpr(p2.second());
        Pair<Token, ParseContext> p4 = expectConsume(p3.second(), Token.Kind.DOT);

        ImmSeq<Token> names = p1.first();
        Token colon = p2.first();
        Expr type = p3.first();
        Token dot = p4.first();

        return Pair.of(new Command.Axiom(names, type, colon, axiomTok, dot), p4.second());
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
    parseDefinition(Token defTok, ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p1 = expectConsume(ctx, Token.Kind.IDENT);
        Pair<ImmSeq<ParamGroup>, ParseContext> p2 = parseParamGroupList(p1.second());
        Pair<Token, ParseContext> p3 = expectConsume(p2.second(), Token.Kind.COLON);
        Pair<Expr, ParseContext> p4 = parseExpr(p3.second());
        Pair<Token, ParseContext> p5 = expectConsume(p4.second(), Token.Kind.COLON_EQ);
        Pair<Expr, ParseContext> p6 = parseExpr(p5.second());
        Pair<Token, ParseContext> p7 = expectConsume(p6.second(), Token.Kind.DOT);

        Token name = p1.first();
        ImmSeq<ParamGroup> paramGroups = p2.first();
        Token colon = p3.first();
        Expr type = p4.first();
        Token assign = p5.first();
        Expr body = p6.first();
        Token dot = p7.first();

        return Pair.of(new Command.Definition(name, paramGroups, type, body,
                                              defTok, colon, assign, dot),
                       p7.second());
    }

    private static @NotNull Pair<Command.@NotNull Notation, @NotNull ParseContext>
    parseNotation(Token notationTok, ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p1 = expectConsume(ctx, Token.Kind.IDENT);
        Token assocToken = p1.first();
        Operator.Assoc assoc = parseAssoc(assocToken);

        Pair<Token, ParseContext> p2 = expectConsume(p1.second(), Token.Kind.LIT_NAT);
        Token.LitNat precToken = (Token.LitNat) p2.first();
        int prec;
        try {
            prec = precToken.value().intValueExact();
        } catch (ArithmeticException e) {
            throw new ParseException(precToken.range(),
                                     "Precedence value out of range: " + precToken.value());
        }

        Pair<Token, ParseContext> p3 = expectConsume(p2.second(), Token.Kind.L_PAREN);
        Pair<Token, ParseContext> p4 = expectConsume(p3.second(), Token.Kind.IDENT);
        Pair<Token, ParseContext> p5 = expectConsume(p4.second(), Token.Kind.R_PAREN);
        Pair<Token, ParseContext> p6 = expectConsume(p5.second(), Token.Kind.COLON_EQ);
        Pair<Expr, ParseContext> p7 = parseExpr(p6.second());
        Pair<Token, ParseContext> p8 = expectConsume(p7.second(), Token.Kind.DOT);

        Token lParen = p3.first();
        Token name = p4.first();
        Token rParen = p5.first();
        Token assign = p6.first();
        Expr expr = p7.first();
        Token dot = p8.first();

        return Pair.of(new Command.Notation(name, assoc, prec, expr,
                                            notationTok, assocToken, lParen, rParen, assign, dot),
                       p8.second());
    }

    private static Pair<ImmSeq<ParamGroup>, ParseContext>
    parseParamGroupList(ParseContext ctx) throws ParseException {
        List<ParamGroup> paramGroups = new ArrayList<>();

        while (true) {
            Pair<Token, ParseContext> p = ctx.nextToken();
            Token tok = p.first();
            ParseContext ctx1 = p.second();

            if (tok.kind() != Token.Kind.L_PAREN && tok.kind() != Token.Kind.L_BRACE) {
                return Pair.of(ImmSeq.of(paramGroups), ctx);
            }

            Pair<ParamGroup, ParseContext> p2 = parseParamGroup(tok, ctx1);
            paramGroups.add(p2.first());
            ctx = p2.second();
        }
    }

    private static Pair<ParamGroup, ParseContext>
    parseParamGroup(Token startToken, ParseContext ctx) throws ParseException {
        Pair<ImmSeq<Token>, ParseContext> p1 = parseIdentList(ctx);
        Pair<Token, ParseContext> p2 = p1.second().nextToken();
        Token colonOrEnd = p2.first();
        if (colonOrEnd.kind() != Token.Kind.COLON) {
            Token.Kind expectedEnd = matchToken(startToken.kind());
            if (colonOrEnd.kind() != matchToken(startToken.kind())) {
                throw expectedAnyOfGot(colonOrEnd, Token.Kind.COLON, expectedEnd);
            }
            Pair<Token, Token> delim = Pair.of(startToken, colonOrEnd);
            return Pair.of(new ParamGroup(p1.first(), null, null, delim), p2.second());
        }

        Pair<Expr, ParseContext> p3 = parseExpr(p2.second());
        Pair<Token, ParseContext> p4 = expectConsume(p3.second(),
                                                     matchToken(startToken.kind()));
        Pair<Token, Token> delim = Pair.of(startToken, p4.first());
        return Pair.of(new ParamGroup(p1.first(), p3.first(), colonOrEnd, delim), p4.second());
    }

    private static Pair<ParamGroup, ParseContext>
    parseSimpleParamGroup(ParseContext ctx) throws ParseException {
        Pair<ImmSeq<Token>, ParseContext> p1 = parseIdentList(ctx);
        Pair<Token, ParseContext> p2 = p1.second().nextToken();
        Token colonOrEnd = p2.first();
        if (colonOrEnd.kind() != Token.Kind.COLON) {
            return Pair.of(new ParamGroup(p1.first(), null, null, null), p2.second());
        }

        Pair<Expr, ParseContext> p3 = parseExpr(p2.second());
        return Pair.of(new ParamGroup(p1.first(), p3.first(), colonOrEnd, null), p3.second());
    }

    private static Pair<ImmSeq<Token>, ParseContext>
    parseIdentList(ParseContext ctx) throws ParseException {
        List<Token> idents = new ArrayList<>();
        while (true) {
            Pair<Token, ParseContext> p = ctx.nextToken();
            Token tok = p.first();
            ParseContext ctx1 = p.second();

            if (tok.kind() != Token.Kind.IDENT) {
                if (idents.isEmpty()) {
                    throw new ParseException(tok.range(), "Expected identifier, got " + tok.kind());
                } else {
                    return Pair.of(ImmSeq.of(idents), ctx);
                }
            }

            idents.add(tok);
            ctx = ctx1;
        }
    }

    private static Pair<Token, ParseContext>
    expectConsume(ParseContext ctx, Token.Kind expected) throws ParseException {
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

    private static Token.Kind matchToken(Token.Kind startKind) {
        return switch (startKind) {
            case Token.Kind.L_PAREN -> Token.Kind.R_PAREN;
            case Token.Kind.L_BRACKET -> Token.Kind.R_BRACKET;
            case Token.Kind.L_BRACE -> Token.Kind.R_BRACE;
            default -> throw new IllegalArgumentException("No matching token for " + startKind);
        };
    }

    private static Operator.Assoc parseAssoc(Token tok) throws ParseException {
        return switch (tok.lexeme()) {
            case "left" -> Operator.Assoc.LEFT;
            case "right" -> Operator.Assoc.RIGHT;
            case "noassoc" -> Operator.Assoc.NONE;
            default -> throw new ParseException(tok.range(),
                                                "Expected 'left', 'right' or 'noassoc', got "
                                                + tok.lexeme());
        };
    }

    private static final Token.Kind[] COMMAND_START = new Token.Kind[] {
        Token.Kind.KW_AXIOM,
        Token.Kind.KW_CHECK,
        Token.Kind.KW_DEFINITION,
        Token.Kind.KW_PROCEDURE,
        Token.Kind.KW_NOTATION
    };
}
