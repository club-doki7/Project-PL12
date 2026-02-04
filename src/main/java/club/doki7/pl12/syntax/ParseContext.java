package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.LexicalException;
import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.UV;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ParseContext(char[] buf,
                           int position,
                           String file,
                           int line,
                           int col,
                           UV<Map<String, Operator.Infix>> infixOps,
                           UV<Set<String>> mixfixFragments)
{
    public static ParseContext of(String content, String file) {
        return new ParseContext(content.toCharArray(),
                                0,
                                file,
                                1,
                                1,
                                UV.of(new HashMap<>()),
                                UV.of(new HashSet<>()));
    }

    public static ParseContext clone(ParseContext ctx, int position, int line, int col) {
        return new ParseContext(ctx.buf,
                                position,
                                ctx.file,
                                line,
                                col,
                                ctx.infixOps,
                                ctx.mixfixFragments);
    }

    public Pair<Token, ParseContext> nextToken() throws LexicalException {
        if (position >= buf.length) {
            return Pair.of(Token.eoi(file, position, line, col), this);
        }

        int position = this.position;
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
    nextTokenImpl(ParseContext ctx, int position, int line, int col) throws LexicalException {
        char[] buf = ctx.buf;
        if (position >= buf.length) {
            return Pair.of(Token.eoi(ctx.file, position, line, col),
                           ParseContext.clone(ctx, position, line, col));
        }

        throw new UnsupportedOperationException();
    }
}
