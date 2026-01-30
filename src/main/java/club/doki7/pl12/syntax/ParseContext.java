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
                                new UV<>(new HashMap<>()),
                                new UV<>(new HashSet<>()));
    }

    public Pair<Token, ParseContext> nextToken() throws LexicalException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ParseContext skipToNextLine() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
