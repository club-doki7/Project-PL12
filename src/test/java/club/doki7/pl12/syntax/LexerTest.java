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
        assertEquals(List.of(sym(Kind.EOI)), tokenize(""));
    }

    @Test
    void testWhitespaceOnly() throws LexicalException {
        assertEquals(List.of(sym(Kind.EOI)), tokenize("   \n\t  "));
    }

    @Test
    void testIdentifier() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar_baz"), ident("x'"), sym(Kind.EOI)),
            tokenize("foo bar_baz x'")
        );
    }

    @Test
    void testKeywords() throws LexicalException {
        assertEquals(
            List.of(
                    sym(Kind.PI),
                    sym(Kind.KW_AXIOM),
                    sym(Kind.KW_DEFUN),
                    sym(Kind.KW_DEFPROC),
                    sym(Kind.KW_LET),
                    sym(Kind.KW_CHECK),
                    sym(Kind.KW_INFIX_L),
                    sym(Kind.KW_INFIX_R),
                    sym(Kind.KW_MIXFIX),
                    eoi()
            ),
            tokenize("forall axiom defun defproc let check infixl infixr mixfix")
        );
    }

    @Test
    void testSymbols() throws LexicalException {
        assertEquals(
            List.of(
                    sym(Kind.L_PAREN),
                    sym(Kind.R_PAREN),
                    sym(Kind.DOT),
                    sym(Kind.COMMA),
                    sym(Kind.COLON),
                    sym(Kind.LAMBDA),
                    sym(Kind.LAMBDA),
                    sym(Kind.PI),
                    sym(Kind.PI),
                    sym(Kind.ARROW),
                    sym(Kind.ARROW),
                    sym(Kind.D_QUES),
                    sym(Kind.ASTER),
                    eoi()
            ),
            tokenize("( ) . , : \\ λ ∀ Π → -> ?? *")
        );
    }

    @Test
    void testNaturalNumbers() throws LexicalException {
        assertEquals(
            List.of(
                    nat(BigInteger.ZERO),
                    nat(BigInteger.valueOf(42)),
                    nat(new BigInteger("12345678901234567890")),
                    eoi()
            ),
            tokenize("0 42 12345678901234567890")
        );
    }

    @Test
    void testStringLiterals() throws LexicalException {
        assertEquals(
            List.of(string("hello"), string("world"), sym(Kind.EOI)),
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
                    eoi()
            ),
            tokenize("\"hello\\nworld\" \"tab\\there\" \"quote\\\"inside\" \"backslash\\\\end\"")
        );
    }

    @Test
    void testBlockComment() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar"), sym(Kind.EOI)),
            tokenize("foo (* this is a comment *) bar")
        );
    }

    @Test
    void testMultipleComments() throws LexicalException {
        assertEquals(
            List.of(ident("foo"), ident("bar"), sym(Kind.EOI)),
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
                    sym(Kind.LAMBDA),
                    ident("x"),
                    sym(Kind.COLON),
                    sym(Kind.ASTER),
                    sym(Kind.DOT),
                    ident("x"),
                    eoi()
            ),
            tokenize("λx : * . x")
        );
    }

    @Test
    void testPiType() throws LexicalException {
        assertEquals(
            List.of(
                    sym(Kind.PI),
                    sym(Kind.L_PAREN),
                    ident("x"),
                    sym(Kind.COLON),
                    ident("A"),
                    sym(Kind.R_PAREN),
                    sym(Kind.ARROW),
                    ident("B"),
                    eoi()
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
            List.of(ident("+"), ident("-"), ident("++"), ident("--"), sym(Kind.EOI)),
            tokenize("+ - ++ --")
        );
    }

    @Test
    void testInfixDeclaration() throws LexicalException {
        assertEquals(
            List.of(
                    sym(Kind.KW_INFIX_L),
                    nat(BigInteger.valueOf(50)),
                    ident("+"),
                    sym(Kind.COLON_EQ),
                    ident("add"),
                    eoi()
            ),
            tokenize("infixl 50 + := add")
        );
    }

    @Test
    void testHoleToken() throws LexicalException {
        assertEquals(
            List.of(sym(Kind.D_QUES), sym(Kind.EOI)),
            tokenize("??")
        );
    }
}
