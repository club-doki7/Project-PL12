package club.doki7.pl12.syntax;

import club.doki7.pl12.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParseContextTest {
    private List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        ParseContext ctx = ParseContext.of(source, "<test>");
        while (true) {
            Pair<Token, ParseContext> result = ctx.nextToken();
            Token token = result.first();
            tokens.add(token);
            if (token.kind == Token.Kind.EOI) {
                break;
            }
            ctx = result.second();
        }
        return tokens;
    }

    static void assertEndsWithEOI(List<Token> tokens) {
        assertEquals(Token.Kind.EOI, tokens.getLast().kind);
    }

    @Test
    void testSimpleIdentifiers() {
        List<Token> tokens = tokenize("foo bar baz");
        assertEquals(4, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("foo", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("bar", tokens.get(1).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(2).kind);
        assertEquals("baz", tokens.get(2).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testKeywords() {
        List<Token> tokens = tokenize("axiom defun defproc let check forall");
        assertEquals(7, tokens.size());
        assertEquals(Token.Kind.KW_AXIOM, tokens.get(0).kind);
        assertEquals(Token.Kind.KW_DEFUN, tokens.get(1).kind);
        assertEquals(Token.Kind.KW_DEFPROC, tokens.get(2).kind);
        assertEquals(Token.Kind.KW_LET, tokens.get(3).kind);
        assertEquals(Token.Kind.KW_CHECK, tokens.get(4).kind);
        assertEquals(Token.Kind.PI, tokens.get(5).kind);  // forall -> PI

        assertEndsWithEOI(tokens);
    }

    @Test
    void testSingleCharSymbols() {
        List<Token> tokens = tokenize("( ) . , * : =");
        assertEquals(8, tokens.size());
        assertEquals(Token.Kind.LPAREN, tokens.get(0).kind);
        assertEquals(Token.Kind.RPAREN, tokens.get(1).kind);
        assertEquals(Token.Kind.DOT, tokens.get(2).kind);
        assertEquals(Token.Kind.COMMA, tokens.get(3).kind);
        assertEquals(Token.Kind.ASTER, tokens.get(4).kind);
        assertEquals(Token.Kind.COLON, tokens.get(5).kind);
        assertEquals(Token.Kind.EQ, tokens.get(6).kind);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testLambdaAndArrow() {
        List<Token> tokens = tokenize("λ \\ -> →");
        assertEquals(5, tokens.size());
        assertEquals(Token.Kind.LAMBDA, tokens.get(0).kind);
        assertEquals("λ", tokens.get(0).lexeme);
        assertEquals(Token.Kind.LAMBDA, tokens.get(1).kind);
        assertEquals("\\", tokens.get(1).lexeme);
        assertEquals(Token.Kind.ARROW, tokens.get(2).kind);
        assertEquals("->", tokens.get(2).lexeme);
        assertEquals(Token.Kind.ARROW, tokens.get(3).kind);
        assertEquals("→", tokens.get(3).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testPiSymbols() {
        List<Token> tokens = tokenize("∀ Π forall");
        assertEquals(4, tokens.size());
        assertEquals(Token.Kind.PI, tokens.get(0).kind);
        assertEquals("∀", tokens.get(0).lexeme);
        assertEquals(Token.Kind.PI, tokens.get(1).kind);
        assertEquals("Π", tokens.get(1).lexeme);
        assertEquals(Token.Kind.PI, tokens.get(2).kind);
        assertEquals("forall", tokens.get(2).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testDoubleQuestion() {
        List<Token> tokens = tokenize("?? ?foo");
        assertEquals(3, tokens.size());
        assertEquals(Token.Kind.DQUES, tokens.get(0).kind);
        assertEquals("??", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("?foo", tokens.get(1).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testSingleLineComment() {
        List<Token> tokens = tokenize("foo -- this is a comment\nbar");
        assertEquals(3, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("foo", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("bar", tokens.get(1).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testBlockComment() {
        List<Token> tokens = tokenize("foo {- this is a\nmultiline comment -} bar");
        assertEquals(3, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("foo", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("bar", tokens.get(1).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testNestedBlockComment() {
        List<Token> tokens = tokenize("foo {- outer {- inner -} outer -} bar");
        assertEquals(3, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("foo", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("bar", tokens.get(1).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testLambdaExpression() {
        List<Token> tokens = tokenize("λx. x");
        assertEquals(5, tokens.size());
        assertEquals(Token.Kind.LAMBDA, tokens.get(0).kind);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("x", tokens.get(1).lexeme);
        assertEquals(Token.Kind.DOT, tokens.get(2).kind);
        assertEquals(Token.Kind.IDENT, tokens.get(3).kind);
        assertEquals("x", tokens.get(3).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testPiType() {
        List<Token> tokens = tokenize("∀(x : A). B");

        assertEquals(9, tokens.size());
        assertEquals(Token.Kind.PI, tokens.get(0).kind);
        assertEquals(Token.Kind.LPAREN, tokens.get(1).kind);
        assertEquals(Token.Kind.IDENT, tokens.get(2).kind);
        assertEquals("x", tokens.get(2).lexeme);
        assertEquals(Token.Kind.COLON, tokens.get(3).kind);
        assertEquals(Token.Kind.IDENT, tokens.get(4).kind);
        assertEquals("A", tokens.get(4).lexeme);
        assertEquals(Token.Kind.RPAREN, tokens.get(5).kind);
        assertEquals(Token.Kind.DOT, tokens.get(6).kind);
        assertEquals(Token.Kind.IDENT, tokens.get(7).kind);
        assertEquals("B", tokens.get(7).lexeme);
        assertEquals(Token.Kind.EOI, tokens.get(8).kind);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testLineAndColumnTracking() {
        List<Token> tokens = tokenize("foo\nbar baz");
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals(1, tokens.get(0).line);
        assertEquals(1, tokens.get(0).col);

        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals(2, tokens.get(1).line);
        assertEquals(1, tokens.get(1).col);

        assertEquals(Token.Kind.IDENT, tokens.get(2).kind);
        assertEquals(2, tokens.get(2).line);
        assertEquals(5, tokens.get(2).col);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testInfixOperator() {
        HashMap<String, Operator.Infix> infixOps = new HashMap<>();
        infixOps.put("+", new Operator.Infix("+", 6, Operator.Assoc.LEFT));

        ParseContext ctx = ParseContext.of("a + b", "<test>", infixOps, new HashSet<>());

        Pair<Token, ParseContext> r1 = ctx.nextToken();
        assertEquals(Token.Kind.IDENT, r1.first().kind);
        assertEquals("a", r1.first().lexeme);

        Pair<Token, ParseContext> r2 = r1.second().nextToken();
        assertEquals(Token.Kind.BINARY, r2.first().kind);
        assertEquals("+", r2.first().lexeme);

        Pair<Token, ParseContext> r3 = r2.second().nextToken();
        assertEquals(Token.Kind.IDENT, r3.first().kind);
        assertEquals("b", r3.first().lexeme);

        Pair<Token, ParseContext> r4 = r3.second().nextToken();
        assertEquals(Token.Kind.EOI, r4.first().kind);
    }

    @Test
    void testMixfixFragment() {
        HashSet<String> mixfixFragments = new HashSet<>();
        mixfixFragments.add("if");
        mixfixFragments.add("then");
        mixfixFragments.add("else");

        ParseContext ctx = ParseContext.of("if a then b else c", "<test>", new HashMap<>(), mixfixFragments);

        Pair<Token, ParseContext> r1 = ctx.nextToken();
        assertEquals(Token.Kind.MIXFIX_FRAG, r1.first().kind);
        assertEquals("if", r1.first().lexeme);

        Pair<Token, ParseContext> r2 = r1.second().nextToken();
        assertEquals(Token.Kind.IDENT, r2.first().kind);
        assertEquals("a", r2.first().lexeme);

        Pair<Token, ParseContext> r3 = r2.second().nextToken();
        assertEquals(Token.Kind.MIXFIX_FRAG, r3.first().kind);
        assertEquals("then", r3.first().lexeme);

        Pair<Token, ParseContext> r4 = r3.second().nextToken();
        assertEquals(Token.Kind.IDENT, r4.first().kind);
        assertEquals("b", r4.first().lexeme);

        Pair<Token, ParseContext> r5 = r4.second().nextToken();
        assertEquals(Token.Kind.MIXFIX_FRAG, r5.first().kind);
        assertEquals("else", r5.first().lexeme);

        Pair<Token, ParseContext> r6 = r5.second().nextToken();
        assertEquals(Token.Kind.IDENT, r6.first().kind);
        assertEquals("c", r6.first().lexeme);

        Pair<Token, ParseContext> r7 = r6.second().nextToken();
        assertEquals(Token.Kind.EOI, r7.first().kind);
    }

    @Test
    void testIdentifierWithPrime() {
        List<Token> tokens = tokenize("x x' x''");
        assertEquals(4, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("x", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("x'", tokens.get(1).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(2).kind);
        assertEquals("x''", tokens.get(2).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testIdentifierWithUnderscore() {
        List<Token> tokens = tokenize("_foo foo_bar _");
        assertEquals(4, tokens.size());
        assertEquals(Token.Kind.IDENT, tokens.get(0).kind);
        assertEquals("_foo", tokens.get(0).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(1).kind);
        assertEquals("foo_bar", tokens.get(1).lexeme);
        assertEquals(Token.Kind.IDENT, tokens.get(2).kind);
        assertEquals("_", tokens.get(2).lexeme);

        assertEndsWithEOI(tokens);
    }

    @Test
    void testEmptyInput() {
        List<Token> tokens = tokenize("");
        assertEquals(1, tokens.size());
        assertEndsWithEOI(tokens);
    }

    @Test
    void testWhitespaceOnly() {
        List<Token> tokens = tokenize("   \n  \t  ");
        assertEquals(1, tokens.size());
        assertEndsWithEOI(tokens);
    }
}
