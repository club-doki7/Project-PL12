package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.SourceLocation;
import club.doki7.pl12.exc.SourceRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Objects;

public record Token(@NotNull Kind kind,
                    @NotNull String lexeme,
                    @NotNull String file,
                    int pos,
                    int line,
                    int col)
{
    public enum Kind {
        /// 标识符
        IDENT,
        /// 二元运算符
        BINARY,
        /// 混缀运算符片段
        MIXFIX_FRAG,
        /// 左括号
        LPAREN,
        /// 右括号
        RPAREN,
        /// `λ` 或者 `\`
        LAMBDA,
        /// `->` 和 `→`
        ARROW,
        /// `.`
        DOT,
        /// `,`
        COMMA,
        /// `*`
        ASTER,
        /// `∀`, `Π` 或者 `forall`
        PI,
        /// `:`
        COLON,
        /// `=`
        EQ,
        /// `??`
        DQUES,

        /// 外围语言所用的一些关键字
        KW_AXIOM,
        KW_DEFUN,
        KW_DEFPROC,
        KW_LET,
        KW_CHECK,

        /// End Of Input
        EOI;

        @Override
        public @NotNull String toString() {
            return switch (this) {
                case IDENT -> "identifier";
                case BINARY -> "binary-operator";
                case MIXFIX_FRAG -> "mixfix-fragment";
                case LPAREN -> "(";
                case RPAREN -> ")";
                case LAMBDA -> "λ";
                case ARROW -> "→";
                case DOT -> ".";
                case COMMA -> ",";
                case ASTER -> "*";
                case PI -> "∀";
                case COLON -> ":";
                case EQ -> "=";
                case DQUES -> "??";
                case KW_AXIOM -> "axiom";
                case KW_DEFUN -> "defun";
                case KW_DEFPROC -> "defproc";
                case KW_LET -> "let";
                case KW_CHECK -> "check";
                case EOI -> "<EOI>";
            };
        }
    }

    public @NotNull SourceLocation location() {
        return new SourceLocation(file, pos, line, col);
    }

    public @NotNull SourceLocation locationEnd() {
        return new SourceLocation(file, pos + lexeme.length(), line, col + lexeme.length());
    }

    public @NotNull SourceRange range() {
        return new SourceRange(location(), locationEnd());
    }

    @Override
    public @NotNull String toString() {
        return lexeme;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token other = (Token) obj;
        if (this.kind != other.kind) return false;
        if (this.kind == Kind.IDENT) {
            return this.lexeme.equals(other.lexeme);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (this.kind == Kind.IDENT) {
            return Objects.hash(kind, lexeme);
        } else {
            // Token(PI, "Π") == Token(PI, "forall") 但 Token(PI, "Π") != "Π"
            // 所以用第二个空字符串来区分 Token 和 String
            // 虽然按理说不会有人把 Token 和 String 装进同一个 HashMap
            return Objects.hash(kind, "");
        }
    }

    @TestOnly
    public static @NotNull Token ident(@NotNull String lexeme) {
        return new Token(Kind.IDENT, lexeme, "<test>", 0, -1, -1);
    }

    @TestOnly
    public static @NotNull Token symbol(@NotNull Kind kind) {
        return new Token(kind, switch (kind) {
            case LPAREN -> "(";
            case RPAREN -> ")";
            case LAMBDA -> "λ";
            case ARROW -> "→";
            case DOT -> ".";
            case COMMA -> ",";
            case ASTER -> "*";
            case PI -> "∀";
            case COLON -> ":";
            case EQ -> "=";
            case DQUES -> "??";
            case KW_AXIOM -> "axiom";
            case KW_DEFUN -> "defun";
            case KW_DEFPROC -> "defproc";
            case KW_LET -> "let";
            case KW_CHECK -> "check";
            case EOI -> "";

            case IDENT -> throw new IllegalArgumentException("IDENT token requires a lexeme");
            case BINARY -> throw new IllegalArgumentException("BINARY token requires a lexeme");
            case MIXFIX_FRAG -> throw new IllegalArgumentException("MIXFIX_FRAG token"
                                                                   + " requires a lexeme");
        }, "<test>", 0, -1, -1);
    }
}
