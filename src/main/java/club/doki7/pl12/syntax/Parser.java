package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    public static @NotNull Program
    parseProgram(ParseContext ctx) throws ParseException {
        List<Command> commands = new ArrayList<>();

        while (true) {
            Pair<Command, ParseContext> p = parseCommand(ctx);
            Command cmd = p.first();
            ctx = p.second();

            if (cmd == null) {
                break;
            }

            commands.add(cmd);
        }

        return Program.of(commands);
    }

    /// ```bnf
    /// command ::= axiom | check | definition | notation
    /// ```
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
        @Nullable ParseContext.Mode originalMode = ctx.mode();
        if (ctx.mode() != ParseContext.Mode.BVR) {
            ctx = ctx.setMode(ParseContext.Mode.BVR);
        }

        Pair<Expr, ParseContext> p = parseExprImpl(ctx);
        if (originalMode == null) {
            return p;
        }
        return Pair.of(p.first(), p.second().setMode(originalMode));
    }

    /// ```bnf
    /// axiom ::= "Axiom" identifier-list ":" expr "."
    /// ```
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

    /// ```bnf
    /// check ::= "Check" expr "."
    /// ```
    private static @NotNull Pair<Command.@NotNull Check, @NotNull ParseContext>
    parseCheck(Token checkTok, ParseContext ctx) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseExpr(ctx);
        Pair<Token, ParseContext> p2 = expectConsume(p1.second(), Token.Kind.DOT);

        Expr expr = p1.first();
        Token dot = p2.first();
        ParseContext ctx1 = p2.second();

        return Pair.of(new Command.Check(expr, checkTok, dot), ctx1);
    }

    /// ```bnf
    /// definition ::= definition-keyword name param-group* ":" expr ":=" expr "."
    /// definition-keyword ::= "Definition" | "Procedure"
    /// ```
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

    /// ```bnf
    /// notation ::= "Notation" assoc prec "(" name ")" ":=" expr "."
    /// ```
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

    /// ```bnf
    /// expr ::= fun | pi | ann
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseExprImpl(ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p = ctx.nextToken();
        Token tok = p.first();
        return switch (tok.kind()) {
            case Token.Kind.FUN -> parseFun(tok, p.second());
            case Token.Kind.PI -> parsePi(tok, p.second());
            default -> parseAnn(ctx);
        };
    }

    /// ```bnf
    /// fun ::= "fun" param-group+ "=>" expr
    ///       | "fun" simple-param-group "=>" expr
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseFun(Token funTok, ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> peek = ctx.nextToken();
        Token tok = peek.first();

        List<ParamGroup> paramGroups = new ArrayList<>();

        if (tok.kind() == Token.Kind.L_PAREN || tok.kind() == Token.Kind.L_BRACE) {
            while (true) {
                Pair<Token, ParseContext> p = ctx.nextToken();
                Token t = p.first();
                if (t.kind() != Token.Kind.L_PAREN && t.kind() != Token.Kind.L_BRACE) {
                    break;
                }
                Pair<ParamGroup, ParseContext> pg = parseParamGroup(t, p.second());
                paramGroups.add(pg.first());
                ctx = pg.second();
            }
        } else {
            Pair<ParamGroup, ParseContext> pg = parseSimpleParamGroup(ctx);
            paramGroups.add(pg.first());
            ctx = pg.second();
        }

        Pair<Token, ParseContext> p2 = expectConsume(ctx, Token.Kind.D_ARROW);
        Pair<Expr, ParseContext> p3 = parseExprImpl(p2.second());

        return Pair.of(new Expr.Fun(ImmSeq.of(paramGroups), p3.first(), funTok, p2.first()),
                       p3.second());
    }

    /// ```bnf
    /// pi ::= pi-keyword param-group "," expr
    /// pi-keyword ::= "∀" | "Π" | "forall"
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parsePi(Token piTok, ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p1 = ctx.nextToken();
        Token startTok = p1.first();

        ParamGroup paramGroup;
        if (startTok.kind() == Token.Kind.L_PAREN || startTok.kind() == Token.Kind.L_BRACE) {
            Pair<ParamGroup, ParseContext> pg = parseParamGroup(startTok, p1.second());
            paramGroup = pg.first();
            ctx = pg.second();
        } else {
            Pair<ParamGroup, ParseContext> pg = parseSimpleParamGroup(ctx);
            paramGroup = pg.first();
            ctx = pg.second();
        }

        Pair<Token, ParseContext> p2 = expectConsume(ctx, Token.Kind.COMMA);
        Pair<Expr, ParseContext> p3 = parseExprImpl(p2.second());

        return Pair.of(new Expr.Pi(paramGroup, p3.first(), piTok, p2.first()), p3.second());
    }

    /// ```bnf
    /// ann ::= term (":" expr)?
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseAnn(ParseContext ctx) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseArrow(ctx);
        Pair<Token, ParseContext> peek = p1.second().nextToken();

        if (peek.first().kind() == Token.Kind.COLON) {
            Pair<Expr, ParseContext> p2 = parseExprImpl(peek.second());
            return Pair.of(new Expr.Ann(p1.first(), p2.first(), peek.first()), p2.second());
        } else {
            return p1;
        }
    }

    /// ```bnf
    /// arrow ::= term ("->" arrow)?
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseArrow(ParseContext ctx) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseTerm(ctx);
        Expr left = p1.first();

        Pair<Token, ParseContext> peek = p1.second().nextToken();
        if (peek.first().kind() == Token.Kind.ARROW) {
            Pair<Expr, ParseContext> p2 = parseArrow(peek.second());
            Token arrow = peek.first();
            Expr right = p2.first();
            return Pair.of(new Expr.Arrow(left, right, arrow), p2.second());
        } else {
            return Pair.of(left, p1.second());
        }
    }

    /// ```bnf
    /// term ::= app (binary-tail)*
    /// binary-tail ::= op app
    /// ```
    ///
    /// Pratt parsing with operator precedence.
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseTerm(ParseContext ctx) throws ParseException {
        return parseTerm(ctx, Integer.MIN_VALUE);
    }

    @SuppressWarnings("InfiniteRecursion")
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseTerm(ParseContext ctx, int minPrec) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseApp(ctx);
        Expr left = p1.first();
        ctx = p1.second();

        while (true) {
            Pair<Token, ParseContext> peek = ctx.nextToken();
            Token tok = peek.first();

            if (tok.kind() != Token.Kind.INFIX) {
                break;
            }

            Token.Infix infixTok = (Token.Infix) tok;
            Operator op = infixTok.infixOp();

            if (op.prec() < minPrec) {
                break;
            }

            ctx = peek.second();

            int nextMinPrec = switch (op.assoc()) {
                case LEFT, NONE -> op.prec() + 1;
                case RIGHT -> op.prec();
            };

            Pair<Expr, ParseContext> p2 = parseTerm(ctx, nextMinPrec);
            Expr right = p2.first();
            ctx = p2.second();

            Expr opExpr = new Expr.Var(tok);
            left = new Expr.App(opExpr,
                                ImmSeq.of(new Argument.Explicit(left),
                                          new Argument.Explicit(right)),
                                true);
        }

        return Pair.of(left, ctx);
    }

    /// ```bnf
    /// app ::= atom arg*
    /// arg ::= atom | "_" | "{" expr "}" | "{" name "=" expr "}"
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseApp(ParseContext ctx) throws ParseException {
        Pair<Expr, ParseContext> p1 = parseAtom(ctx);
        Expr func = p1.first();
        ctx = p1.second();

        List<Argument> args = new ArrayList<>();

        while (true) {
            Pair<Token, ParseContext> peek = ctx.nextToken();
            Token tok = peek.first();

            if (tok.kind() == Token.Kind.L_BRACE) {
                Pair<Argument, ParseContext> argP = parseImplicitArg(tok, peek.second());
                args.add(argP.first());
                ctx = argP.second();
            } else if (isAtomStart(tok)) {
                Pair<Expr, ParseContext> argExpr = parseAtom(ctx);
                args.add(new Argument.Explicit(argExpr.first()));
                ctx = argExpr.second();
            } else {
                break;
            }
        }

        if (args.isEmpty()) {
            return Pair.of(func, ctx);
        } else {
            return Pair.of(new Expr.App(func, ImmSeq.of(args), false), ctx);
        }
    }

    /// ```bnf
    /// implicit-arg ::= "{" expr "}" | "{" name "=" expr "}"
    /// ```
    private static @NotNull Pair<@NotNull Argument, @NotNull ParseContext>
    parseImplicitArg(Token lbrace, ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p1 = ctx.nextToken();
        Token first = p1.first();

        if (first.kind() == Token.Kind.IDENT) {
            Pair<Token, ParseContext> p2 = p1.second().nextToken();
            if ((p2.first().kind() == Token.Kind.INFIX
                 || p2.first().kind() == Token.Kind.IDENT)
                && p2.first().lexeme().equals("=")) {
                Pair<Expr, ParseContext> p3 = parseExprImpl(p2.second());
                Pair<Token, ParseContext> p4 = expectConsume(p3.second(), Token.Kind.R_BRACE);
                return Pair.of(new Argument.NamedImplicit(first, p3.first(),
                                                          lbrace, p4.first(), p2.first()),
                               p4.second());
            }
        }

        Pair<Expr, ParseContext> exprP = parseExprImpl(ctx);
        Pair<Token, ParseContext> rbrace = expectConsume(exprP.second(), Token.Kind.R_BRACE);
        return Pair.of(new Argument.Implicit(exprP.first(), lbrace, rbrace.first()), rbrace.second());
    }

    private static boolean isAtomStart(Token tok) {
        return switch (tok.kind()) {
            case Token.Kind.UNIV,
                 Token.Kind.IDENT,
                 Token.Kind.LIT_NAT,
                 Token.Kind.LIT_STRING,
                 Token.Kind.D_QUES,
                 Token.Kind.L_PAREN -> true;
            default -> false;
        };
    }

    /// ```bnf
    /// atom ::= univ | var | lit | hole | paren
    /// ```
    private static @NotNull Pair<@NotNull Expr, @NotNull ParseContext>
    parseAtom(ParseContext ctx) throws ParseException {
        Pair<Token, ParseContext> p = ctx.nextToken();
        Token tok = p.first();
        ParseContext ctx1 = p.second();

        return switch (tok.kind()) {
            case Token.Kind.UNIV -> Pair.of(new Expr.Univ(tok), ctx1);
            case Token.Kind.IDENT -> Pair.of(new Expr.Var(tok), ctx1);
            case Token.Kind.LIT_NAT, Token.Kind.LIT_STRING -> Pair.of(new Expr.Lit(tok), ctx1);
            case Token.Kind.D_QUES -> Pair.of(new Expr.Hole(tok), ctx1);
            case Token.Kind.L_PAREN -> {
                Pair<Expr, ParseContext> innerP = parseExprImpl(ctx1);
                Pair<Token, ParseContext> rparenP = expectConsume(innerP.second(), Token.Kind.R_PAREN);
                yield Pair.of(new Expr.Paren(innerP.first(), tok, rparenP.first()), rparenP.second());
            }
            default -> throw new ParseException(tok.range(),
                                                "Expected expression, got " + tok.kind());
        };
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

        Pair<Expr, ParseContext> p3 = parseExprImpl(p2.second());
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
            return Pair.of(new ParamGroup(p1.first(), null, null, null), p1.second());
        }

        Pair<Expr, ParseContext> p3 = parseExprImpl(p2.second());
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
