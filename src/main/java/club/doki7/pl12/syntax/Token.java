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
        /// `λ` 和 `\`
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
        /// `:=`
        COLON_EQ,
        /// `??`
        D_QUES,

        /// 外围语言所用的一些关键字
        KW_AXIOM,
        KW_DEFINITION,
        KW_PROCEDURE,
        KW_CHECK,
        KW_NOTATION,

        /// End Of Input
        EOI;

        public static final Map<String, Kind> KEYWORDS_MAP = Map.ofEntries(
            Map.entry("forall", PI),

            Map.entry("Axiom", KW_AXIOM),
            Map.entry("Definition", KW_DEFINITION),
            Map.entry("Procedure", KW_PROCEDURE),
            Map.entry("Check", KW_CHECK),
            Map.entry("Notation", KW_NOTATION)
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

    record Prefix(@NotNull Kind kind,
                  @NotNull Operator.Prefix prefixOp,
                  @NotNull String file,
                  int pos,
                  int line,
                  int col)
        implements Token
    {
        @Override
        public @NotNull String lexeme() {
            return prefixOp.lexeme();
        }

        @Override
        public @NotNull String toString() {
            return lexeme();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Prefix(Kind kind1, Operator.Prefix prefixOp1, _, _, _, _))) {
                return false;
            }
            return kind == kind1 && prefixOp.equals(prefixOp1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Prefix.class, kind, prefixOp);
        }
    }

    record Infix(@NotNull Kind kind,
                 @NotNull Operator.Infix infixOp,
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
            if (!(obj instanceof Infix(Kind kind1, Operator.Infix infixOp1, _, _, _, _))) {
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

    static Token prefixOp(@NotNull Operator.Prefix prefixOp,
                          @NotNull String file,
                          int pos,
                          int line,
                          int col)
    {
        return new Prefix(Kind.INFIX, prefixOp, file, pos, line, col);
    }

    static Token infixOp(@NotNull Operator.Infix infixOp,
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
    static Token prefixOp(@NotNull Operator.Prefix prefixOp) {
        return new Prefix(Kind.PREFIX, prefixOp, "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token infixOp(@NotNull Operator.Infix infixOp) {
        return new Infix(Kind.INFIX, infixOp, "<test>", 0, 0, 0);
    }

    @TestOnly
    static Token eoi() {
        return new Simple(Kind.EOI, "<EOI>", "<test>", 0, 0, 0);
    }
}
