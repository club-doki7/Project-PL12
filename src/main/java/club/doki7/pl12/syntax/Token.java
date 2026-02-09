package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.SourceLocation;
import club.doki7.pl12.exc.SourceRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

public sealed interface Token {
    enum Kind {
        /// 标识符
        IDENT,
        /// 前缀运算符
        PREFIX,
        /// 中缀运算符
        INFIX,
        /// 字符串
        LIT_STRING,
        /// 自然数
        LIT_NAT,
        /// 左括号
        L_PAREN,
        /// 右括号
        R_PAREN,
        /// 左中括号
        L_BRACKET,
        /// 右中括号
        R_BRACKET,
        /// 左大括号
        L_BRACE,
        /// 右大括号
        R_BRACE,
        /// `λ` 和 `fun`
        FUN,
        /// `->` 和 `→`
        ARROW,
        /// `=>` 和 `⇒`
        D_ARROW,
        /// `.`
        DOT,
        /// `,`
        COMMA,
        /// `𝒰` 或者 `*`
        UNIV,
        /// `∀`, `Π` 或者 `forall`
        PI,
        /// `:`
        COLON,
        /// `:=`
        COLON_EQ,
        /// `??`
        D_QUES,

        // IDLE 状态的关键字
        KW_AXIOM,
        KW_DEFINITION,
        KW_PROCEDURE,
        KW_CHECK,
        KW_NOTATION,

        // BVR 状态的关键字，暂无
        KW_BVR_NONE,

        // DOGFIGHT 状态的关键字
        KW_DF_LET,
        KW_DF_IN,
        KW_DF_IF,
        KW_DF_THEN,
        KW_DF_ELSE,
        KW_DF_CASE,
        KW_DF_OF,
        KW_DF_LOOP,
        KW_DF_BREAK,
        KW_DF_CONTINUE,
        KW_DF_RETURN,
        KW_DF_END,

        /// End Of Input
        EOI;

        public static final Map<String, Kind> KEYWORDS_MAP = Map.ofEntries(
            Map.entry("forall", PI),
            Map.entry("fun", FUN),
            Map.entry("𝒰", UNIV),

            Map.entry("Axiom", KW_AXIOM),
            Map.entry("Definition", KW_DEFINITION),
            Map.entry("Procedure", KW_PROCEDURE),
            Map.entry("Check", KW_CHECK),
            Map.entry("Notation", KW_NOTATION)
        );

        public static final Map<String, Kind> DF_KEYWORDS_MAP = Map.ofEntries(
            Map.entry("let", KW_DF_LET),
            Map.entry("in", KW_DF_IN),
            Map.entry("if", KW_DF_IF),
            Map.entry("then", KW_DF_THEN),
            Map.entry("else", KW_DF_ELSE),
            Map.entry("case", KW_DF_CASE),
            Map.entry("of", KW_DF_OF),
            Map.entry("loop", KW_DF_LOOP),
            Map.entry("break", KW_DF_BREAK),
            Map.entry("continue", KW_DF_CONTINUE),
            Map.entry("return", KW_DF_RETURN),
            Map.entry("end", KW_DF_END)
        );
    }

    @NotNull Kind kind();

    @NotNull String lexeme();

    @NotNull String file();

    int pos();

    int line();

    int col();

    default @NotNull SourceLocation location() {
        return new SourceLocation(file(), pos(), line(), col());
    }

    default @NotNull SourceLocation locationEnd() {
        int len = lexeme().length();
        return new SourceLocation(file(), pos() + len, line(), col() + len);
    }

    default @NotNull SourceRange range() {
        return new SourceRange(location(), locationEnd());
    }

    @Override
    @NotNull String toString();

    record Simple(@NotNull Kind kind,
                  @NotNull String lexeme,
                  @NotNull String file,
                  int pos,
                  int line,
                  int col)
        implements Token
    {
        @Override
        public @NotNull String toString() {
            return lexeme();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Simple(Kind kind1, String lexeme1, _, _, _, _))) return false;
            if (kind == Kind.IDENT) {
                return kind == kind1 && lexeme.equals(lexeme1);
            } else {
                return kind == kind1;
            }
        }

        @Override
        public int hashCode() {
            if (kind == Kind.IDENT) {
                return Objects.hash(Simple.class, kind, lexeme);
            } else {
                return Objects.hash(Simple.class, kind);
            }
        }
    }

    record LitString(@NotNull Kind kind,
                     @NotNull String string,
                     @NotNull String lexeme,
                     @NotNull String file,
                     int pos,
                     int line,
                     int col)
        implements Token
    {
        @Override
        public @NotNull String toString() {
            return lexeme;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof LitString(Kind kind1, String string1, _, _, _, _, _))) {
                return false;
            }
            return kind == kind1 && string.equals(string1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(LitString.class, kind, string);
        }
    }

    record LitNat(@NotNull Kind kind,
                  @NotNull BigInteger value,
                  @NotNull String lexeme,
                  @NotNull String file,
                  int pos,
                  int line,
                  int col)
        implements Token
    {
        @Override
        public @NotNull String toString() {
            return lexeme;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof LitNat(Kind kind1, BigInteger value1, _, _, _, _, _))) {
                return false;
            }
            return kind == kind1 && value.equals(value1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(LitNat.class, kind, value);
        }
    }

    record Infix(@NotNull Kind kind,
                 @NotNull Operator infixOp,
                 @NotNull String file,
                 int pos,
                 int line,
                 int col)
        implements Token
    {
        @Override
        public @NotNull String lexeme() {
            return infixOp.lexeme();
        }

        @Override
        public @NotNull String toString() {
            return lexeme();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Infix(Kind kind1, Operator infixOp1, _, _, _, _))) {
                return false;
            }
            return kind == kind1 && infixOp.equals(infixOp1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Infix.class, kind, infixOp);
        }
    }

    static Token ident(@NotNull String lexeme, @NotNull String file, int pos, int line, int col) {
        return new Simple(Kind.IDENT, lexeme, file, pos, line, col);
    }

    static Token string(@NotNull String string,
                        @NotNull String lexeme,
                        @NotNull String file,
                        int pos,
                        int line,
                        int col)
    {
        return new LitString(Kind.LIT_STRING, string, lexeme, file, pos, line, col);
    }

    static Token nat(@NotNull BigInteger value,
                     @NotNull String lexeme,
                     @NotNull String file,
                     int pos,
                     int line,
                     int col)
    {
        return new LitNat(Kind.LIT_NAT, value, lexeme, file, pos, line, col);
    }

    static Token sym(@NotNull Kind kind,
                     @NotNull String lexeme,
                     @NotNull String file,
                     int pos,
                     int line,
                     int col)
    {
        return new Simple(kind, lexeme, file, pos, line, col);
    }

    static Token infixOp(@NotNull Operator infixOp,
                         @NotNull String file,
                         int pos,
                         int line,
                         int col)
    {
        return new Infix(Kind.INFIX, infixOp, file, pos, line, col);
    }

    static Token eoi(@NotNull String file, int pos, int line, int col) {
        return new Simple(Kind.EOI, "<EOI>", file, pos, line, col);
    }

    @TestOnly
    static Token ident(@NotNull String lexeme) {
        return new Simple(Kind.IDENT, lexeme, "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token sym(@NotNull Kind kind) {
        return new Simple(kind, kind.toString(), "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token string(@NotNull String string) {
        return new LitString(Kind.LIT_STRING, string, "\"" + string + "\"", "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token nat(@NotNull BigInteger value) {
        return new LitNat(Kind.LIT_NAT, value, value.toString(), "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token infixOp(@NotNull Operator infixOp) {
        return new Infix(Kind.INFIX, infixOp, "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token eoi() {
        return new Simple(Kind.EOI, "<EOI>", "<test>", 0, 0, 0);
    }
}
