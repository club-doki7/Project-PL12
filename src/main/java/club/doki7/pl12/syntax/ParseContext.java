package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.LexicalException;
import club.doki7.pl12.exc.SourceRange;
import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.UV;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record ParseContext(char[] buf,
                           int pos,
                           String file,
                           int line,
                           int col,
                           UV<Map<String, Operator.Infix>> infixOps)
{
    public static ParseContext of(String content, String file) {
        return new ParseContext(content.toCharArray(),
                                0,
                                file,
                                1,
                                1,
                                UV.of(new HashMap<>()));
    }

    public static ParseContext clone(ParseContext ctx, int position, int line, int col) {
        return new ParseContext(ctx.buf, position, ctx.file, line, col, ctx.infixOps);
    }

    public Pair<Token, ParseContext> nextToken() throws LexicalException {
        if (pos >= buf.length) {
            return Pair.of(Token.eoi(file, pos, line, col), this);
        }

        int position = this.pos;
        int line = this.line;
        int col = this.col;

        while (Character.isWhitespace(buf[position])) {
            if (buf[position] == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            position++;
        }

        return nextTokenImpl(this, position, line, col);
    }

    private static Pair<Token, ParseContext>
    nextTokenImpl(ParseContext ctx, int pos, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;
        if (pos >= buf.length) {
            return Pair.of(Token.eoi(ctx.file, pos, line, col),
                           ParseContext.clone(ctx, pos, line, col));
        }

        return switch (buf[pos]) {
            case '0', '1', '2', '3', '4',
                 '5', '6', '7', '8', '9' -> nextNat(ctx, pos, line, col);
            case '"' -> nextString(ctx, pos, line, col);
            case ')', '[', ']', '{', '}',
                 '→', '.', ',', '∀', 'Π',
                 'λ', '\\', '=', '*' -> nextSingleChar(ctx, pos, line, col);
            case ':' -> {
                if (pos + 1 < buf.length && buf[pos + 1] == '=') {
                    yield Pair.of(Token.sym(Token.Kind.COLON_EQ, ":=", ctx.file, pos, line, col),
                                  ParseContext.clone(ctx, pos + 2, line, col + 2));
                } else {
                    yield Pair.of(Token.sym(Token.Kind.COLON, ":", ctx.file, pos, line, col),
                                  ParseContext.clone(ctx, pos + 1, line, col + 1));
                }
            }
            case '-' -> {
                if (pos + 1 < buf.length && buf[pos + 1] == '>') {
                    yield Pair.of(Token.sym(Token.Kind.ARROW, "->", ctx.file, pos, line, col),
                                  ParseContext.clone(ctx, pos + 2, line, col + 2));
                } else {
                    yield nextIdent(ctx, pos, line, col);
                }
            }
            case '?' -> {
                if (pos + 1 < buf.length && buf[pos + 1] == '?') {
                    yield Pair.of(Token.sym(Token.Kind.D_QUES, "??", ctx.file, pos, line, col),
                                  ParseContext.clone(ctx, pos + 2, line, col + 2));
                } else {
                    throw new LexicalException(SourceRange.of(ctx.file, pos, line, col),
                                               "Identifiers cannot start with '?'."
                                               + " Meanwhile, to input a hole, use '??'.");
                }
            }
            case '(' -> {
                if (pos + 1 < buf.length && buf[pos + 1] == '*') {
                    yield skipComment(ctx, pos, line, col);
                } else {
                    yield Pair.of(Token.sym(Token.Kind.L_PAREN, "(", ctx.file, pos, line, col),
                                  ParseContext.clone(ctx, pos + 1, line, col + 1));
                }
            }
            default -> nextIdent(ctx, pos, line, col);
        };
    }

    public static Pair<Token, ParseContext>
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
                                       "Unexpected character: '" + buf[startPos] + "'");
        }

        @Nullable Operator.Infix infix = ctx.infixOps.value.get(lexeme);
        if (infix != null) {
            return Pair.of(Token.infixOp(infix, ctx.file, startPos, line, startCol),
                           ParseContext.clone(ctx, pos, line, col));
        }

        return Pair.of(Token.ident(lexeme, ctx.file, startPos, line, startCol),
                       ParseContext.clone(ctx, pos, line, col));
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
}
