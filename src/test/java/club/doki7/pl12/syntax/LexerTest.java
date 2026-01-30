package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.LexicalException;
import club.doki7.pl12.util.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static club.doki7.pl12.syntax.Token.*;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {
    private List<Token> tokenize(String input) throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        ParseContext ctx = ParseContext.of(input, "<test>");

        while (true) {
            Pair<Token, ParseContext> result = ctx.nextToken();
            Token token = result.first();
            tokens.add(token);

            if (token.kind() == Kind.EOI) {
                break;
            }
            ctx = result.second();
        }

        return tokens;
    }

    @Test
    void testEmptyInput() throws LexicalException {
        assertEquals(List.of(symbol(Kind.EOI)), tokenize(""));
    }

    @Test
    void testWhitespaceOnly() throws LexicalException {
        assertEquals(List.of(symbol(Kind.EOI)), tokenize("   \n\t  "));
    }

    @Test
    void testIdentifier() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar_baz"), ident("x'"), symbol(Kind.EOI)),
            tokenize("foo bar_baz x'")
        );
    }

    @Test
    void testKeywords() throws LexicalException {
        assertEquals(
            List.of(
                symbol(Kind.PI),
                symbol(Kind.KW_AXIOM),
                symbol(Kind.KW_DEFUN),
                symbol(Kind.KW_DEFPROC),
                symbol(Kind.KW_LET),
                symbol(Kind.KW_CHECK),
                symbol(Kind.KW_INFIX_L),
                symbol(Kind.KW_INFIX_R),
                symbol(Kind.KW_MIXFIX),
                symbol(Kind.EOI)
            ),
            tokenize("forall axiom defun defproc let check infixl infixr mixfix")
        );
    }

    @Test
    void testSymbols() throws LexicalException {
        assertEquals(
            List.of(
                symbol(Kind.L_PAREN),
                symbol(Kind.R_PAREN),
                symbol(Kind.DOT),
                symbol(Kind.COMMA),
                symbol(Kind.COLON),
                symbol(Kind.EQ),
                symbol(Kind.LAMBDA),
                symbol(Kind.LAMBDA),
                symbol(Kind.PI),
                symbol(Kind.PI),
                symbol(Kind.ARROW),
                symbol(Kind.ARROW),
                symbol(Kind.D_QUES),
                symbol(Kind.ASTER),
                symbol(Kind.EOI)
            ),
            tokenize("( ) . , : = \\ λ ∀ Π → -> ?? *")
        );
    }

    @Test
    void testNaturalNumbers() throws LexicalException {
        assertEquals(
            List.of(
                nat(BigInteger.ZERO),
                nat(BigInteger.valueOf(42)),
                nat(new BigInteger("12345678901234567890")),
                symbol(Kind.EOI)
            ),
            tokenize("0 42 12345678901234567890")
        );
    }

    @Test
    void testStringLiterals() throws LexicalException {
        assertEquals(
            List.of(string("hello"), string("world"), symbol(Kind.EOI)),
            tokenize("\"hello\" \"world\"")
        );
    }

    @Test
    void testStringEscapeSequences() throws LexicalException {
        assertEquals(
            List.of(
                string("hello\nworld"),
                string("tab\there"),
                string("quote\"inside"),
                string("backslash\\end"),
                symbol(Kind.EOI)
            ),
            tokenize("\"hello\\nworld\" \"tab\\there\" \"quote\\\"inside\" \"backslash\\\\end\"")
        );
    }

    @Test
    void testBlockComment() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar"), symbol(Kind.EOI)),
            tokenize("foo (* this is a comment *) bar")
        );
    }

    @Test
    void testMultipleComments() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar"), symbol(Kind.EOI)),
            tokenize("(* first *) foo (* second *) (* third *) bar")
        );
    }

    @Test
    void testUnclosedComment() {
        assertThrows(LexicalException.class, () -> tokenize("(* unclosed comment"));
    }

    @Test
    void testUnclosedString() {
        assertThrows(LexicalException.class, () -> tokenize("\"unclosed string"));
    }

    @Test
    void testNewlineInString() {
        assertThrows(LexicalException.class, () -> tokenize("\"hello\nworld\""));
    }

    @Test
    void testUnknownEscapeSequence() {
        assertThrows(LexicalException.class, () -> tokenize("\"hello\\x\""));
    }

    @Test
    void testComplexExpression() throws LexicalException {
        assertEquals(
            List.of(
                symbol(Kind.LAMBDA),
                ident("x"),
                symbol(Kind.COLON),
                symbol(Kind.ASTER),
                symbol(Kind.DOT),
                ident("x"),
                symbol(Kind.EOI)
            ),
            tokenize("λx : * . x")
        );
    }

    @Test
    void testPiType() throws LexicalException {
        assertEquals(
            List.of(
                symbol(Kind.PI),
                symbol(Kind.L_PAREN),
                ident("x"),
                symbol(Kind.COLON),
                ident("A"),
                symbol(Kind.R_PAREN),
                symbol(Kind.ARROW),
                ident("B"),
                symbol(Kind.EOI)
            ),
            tokenize("∀(x : A) -> B")
        );
    }

    @Test
    void testSourceLocation() throws LexicalException {
        ParseContext ctx = ParseContext.of("foo\nbar", "<test>");

        Pair<Token, ParseContext> result1 = ctx.nextToken();
        Token tok1 = result1.first();
        assertEquals(1, tok1.line());
        assertEquals(1, tok1.col());

        Pair<Token, ParseContext> result2 = result1.second().nextToken();
        Token tok2 = result2.first();
        assertEquals(2, tok2.line());
        assertEquals(1, tok2.col());
    }

    @Test
    void testOperatorChars() throws LexicalException {
        // 运算符字符在没有注册中缀运算符时作为标识符处理
        assertEquals(
            List.of(ident("+"), ident("-"), ident("++"), ident("--"), symbol(Kind.EOI)),
            tokenize("+ - ++ --")
        );
    }

    @Test
    void testInfixDeclaration() throws LexicalException {
        assertEquals(
            List.of(
                symbol(Kind.KW_INFIX_L),
                nat(BigInteger.valueOf(50)),
                ident("+"),
                symbol(Kind.COLON_EQ),
                ident("add"),
                symbol(Kind.EOI)
            ),
            tokenize("infixl 50 + := add")
        );
    }

    @Test
    void testHoleToken() throws LexicalException {
        assertEquals(
            List.of(symbol(Kind.D_QUES), symbol(Kind.EOI)),
            tokenize("??")
        );
    }
}
