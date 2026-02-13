package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.LexicalException;
import club.doki7.pl12.exc.SourceRange;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record ParseContext(char[] buf,
                           int pos,
                           String file,
                           int line,
                           int col,
                           Mode mode,
                           Map<String, Operator> infixOps)
{
    public enum Mode { IDLE, BVR, DOGFIGHT }

    public static ParseContext of(String content, String file) {
        return new ParseContext(content.toCharArray(), 0, file, 1, 1, Mode.IDLE, new HashMap<>());
    }

    public ParseContext clone(int pos1, int line1, int col1) {
        return new ParseContext(buf, pos1, file, line1, col1, mode, infixOps);
    }

    public ParseContext setMode(Mode mode) {
        return new ParseContext(buf, pos, file, line, col, mode, infixOps);
    }

    public Pair<Token, ParseContext> nextToken() throws LexicalException {
        if (pos >= buf.length) {
            return Pair.of(Token.eoi(file, pos, line, col), this);
        }

        return nextTokenImpl(this, pos, line, col);
    }

    public static boolean isIdentChar(char c) {
        return switch (c) {
            case '-', '+', '*', '%', '<',
                 '>', '!', '?', '_', '\'',
                 '#', '@', '$', ';', '^',
                 '/', '\\', '|', '~', '=' -> true;
            default -> Character.isLetterOrDigit(c);
        };
    }

    private static Pair<Token, ParseContext>
    nextTokenImpl(ParseContext ctx, int pos, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;

        while (pos < buf.length && Character.isWhitespace(buf[pos])) {
            if (buf[pos] == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            pos++;
        }

        if (pos >= buf.length) {
            return Pair.of(Token.eoi(ctx.file, pos, line, col),
                           ctx.clone(pos, line, col));
        }

        return switch (buf[pos]) {
            case '0', '1', '2', '3', '4',
                 '5', '6', '7', '8', '9' -> nextNat(ctx, pos, line, col);
            case '"' -> nextString(ctx, pos, line, col);
            case ')', '[', ']', '{', '}',
                 '→', '⇒', '.', ',', 'λ',
                 '∀', 'Π' -> nextSingleChar(ctx, pos, line, col);
            case ':' ->
                (pos + 1 < buf.length && buf[pos + 1] == '=')
                    ? Pair.of(Token.sym(Token.Kind.COLON_EQ, ":=", ctx.file, pos, line, col),
                              ctx.clone(pos + 2, line, col + 2))
                    : Pair.of(Token.sym(Token.Kind.COLON, ":", ctx.file, pos, line, col),
                              ctx.clone(pos + 1, line, col + 1));
            case '-' ->
                (pos + 1 < buf.length && buf[pos + 1] == '>')
                    ? Pair.of(Token.sym(Token.Kind.ARROW, "->", ctx.file, pos, line, col),
                              ctx.clone(pos + 2, line, col + 2))
                    : nextIdent(ctx, pos, line, col);
            case '(' ->
                (pos + 1 < buf.length && buf[pos + 1] == '*')
                    ? skipComment(ctx, pos, line, col)
                    : Pair.of(Token.sym(Token.Kind.L_PAREN, "(", ctx.file, pos, line, col),
                          ctx.clone(pos + 1, line, col + 1));
            case '=' ->
                (pos + 1 < buf.length && buf[pos + 1] == '>')
                    ? Pair.of(Token.sym(Token.Kind.D_ARROW, "=>", ctx.file, pos, line, col),
                              ctx.clone(pos + 2, line, col + 2))
                    : nextIdent(ctx, pos, line, col);
            case '?' -> {
                if (pos + 1 < buf.length && buf[pos + 1] == '?') {
                    yield Pair.of(Token.sym(Token.Kind.D_QUES, "??", ctx.file, pos, line, col),
                                  ctx.clone(pos + 2, line, col + 2));
                } else {
                    throw new LexicalException(SourceRange.of(ctx.file, pos, line, col),
                                               "变量名不能以问号 '?' 开头，"
                                               + "若要输入 Hole，请使用 '??'");
                }
            }
            default -> nextIdent(ctx, pos, line, col);
        };
    }

    private static Pair<Token, ParseContext>
    nextIdent(ParseContext ctx, int pos, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;
        int startPos = pos;
        int startCol = col;

        StringBuilder sb = new StringBuilder();
        while (pos < buf.length && isIdentChar(buf[pos])) {
            sb.append(buf[pos]);
            pos++;
            col++;
        }

        String lexeme = sb.toString();
        if (lexeme.isEmpty()) {
            throw new LexicalException(SourceRange.of(ctx.file, startPos, line, startCol),
                                       "无效的字符：'" + buf[startPos] + "'");
        }

        @Nullable Token.Kind kwKind = Token.Kind.KEYWORDS_MAP.get(lexeme);
        if (kwKind != null) {
            return Pair.of(Token.sym(kwKind, lexeme, ctx.file, startPos, line, startCol),
                           ctx.clone(pos, line, col));
        }

        if (ctx.mode == Mode.DOGFIGHT) {
            kwKind = Token.Kind.BVR_KEYWORDS_MAP.get(lexeme);
            if (kwKind != null) {
                return Pair.of(Token.sym(kwKind, lexeme, ctx.file, startPos, line, startCol),
                               ctx.clone(pos, line, col));
            }
        }

        @Nullable Operator infix = ctx.infixOps.get(lexeme);
        if (infix != null) {
            return Pair.of(Token.infixOp(infix, ctx.file, startPos, line, startCol),
                           ctx.clone(pos, line, col));
        }

        return Pair.of(Token.ident(lexeme, ctx.file, startPos, line, startCol),
                       ctx.clone(pos, line, col));
    }

    private static Pair<Token, ParseContext>
    nextNat(ParseContext ctx, int pos, int line, int col) {
        char[] buf = ctx.buf;
        int startPos = pos;
        int startCol = col;

        StringBuilder sb = new StringBuilder();
        while (pos < buf.length && Character.isDigit(buf[pos])) {
            sb.append(buf[pos]);
            pos++;
            col++;
        }

        String lexeme = sb.toString();
        return Pair.of(Token.nat(new java.math.BigInteger(lexeme),
                                 lexeme,
                                 ctx.file,
                                 startPos,
                                 line,
                                 startCol),
                       ctx.clone(pos, line, col));
    }

    private static Pair<Token, ParseContext>
    nextString(ParseContext ctx, int pos, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;
        int startPos = pos;
        int startCol = col;
        pos++;
        col++;

        StringBuilder sb = new StringBuilder();
        StringBuilder sbLexeme = new StringBuilder();
        while (pos < buf.length && buf[pos] != '"' && buf[pos] != '\n') {
            if (buf[pos] == '\\') {
                pos++;
                col++;
                if (pos >= buf.length) {
                    throw new LexicalException(SourceRange.of(ctx.file, startPos, line, startCol),
                                               "未终止的字符串字面量");
                }
                switch (buf[pos]) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> throw new LexicalException(SourceRange.of(ctx.file, pos, line, col),
                                                          "无效的转义字序列：'\\" + buf[pos] + "'");
                }

                sbLexeme.append('\\').append(buf[pos]);
            } else {
                sb.append(buf[pos]);
                sbLexeme.append(buf[pos]);
            }
            pos++;
            col++;
        }

        if (pos >= buf.length || buf[pos] != '"') {
            throw new LexicalException(SourceRange.of(ctx.file, startPos, line, startCol),
                                       "未终止的字符串字面量");
        }

        String str = sb.toString();
        String lexeme = sbLexeme.toString();
        pos++;
        col++;

        return Pair.of(Token.string(str, lexeme, ctx.file, startPos, line, startCol),
                       ctx.clone(pos, line, col));
    }

    private static Pair<Token, ParseContext>
    nextSingleChar(ParseContext ctx, int pos, int line, int col) {
        char c = ctx.buf[pos];
        Token.Kind kind = switch (c) {
            case ')' -> Token.Kind.R_PAREN;
            case '(' -> Token.Kind.L_PAREN;
            case '[' -> Token.Kind.L_BRACKET;
            case ']' -> Token.Kind.R_BRACKET;
            case '{' -> Token.Kind.L_BRACE;
            case '}' -> Token.Kind.R_BRACE;
            case '→' -> Token.Kind.ARROW;
            case '⇒' -> Token.Kind.D_ARROW;
            case '.' -> Token.Kind.DOT;
            case ',' -> Token.Kind.COMMA;
            case '∀', 'Π' -> Token.Kind.PI;
            case 'λ' -> Token.Kind.FUN;
            default -> throw new IllegalStateException("Unexpected character: " + c);
        };

        return Pair.of(Token.sym(kind, Character.toString(c), ctx.file, pos, line, col),
                       ctx.clone(pos + 1, line, col + 1));
    }

    private static Pair<Token, ParseContext>
    skipComment(ParseContext ctx, int pos, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;
        int startPos = pos;
        int startLine = line;
        int startCol = col;
        pos += 2;

        while (pos + 1 < buf.length) {
            if (buf[pos] == '*' && buf[pos + 1] == ')') {
                pos += 2;
                col += 2;
                return nextTokenImpl(ctx, pos, line, col);
            } else {
                if (buf[pos] == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
                pos++;
            }
        }

        throw new LexicalException(SourceRange.of(ctx.file, startPos, startLine, startCol),
                                   "未终止的注释");
    }
}
