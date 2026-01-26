package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class Token {
    public enum Kind {
        /// 标识符
        IDENT,
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
        /// `∀` 或者 `forall`
        PI,
        /// `:`
        COLON,
        /// `=`
        EQ,
        /// `?`
        QUES,

        /// 外围语言所用的一些关键字
        KW_AXIOM,
        KW_DEFUN,
        KW_DEFPROC,
        KW_LET,
        KW_CHECK;

        @Override
        public @NotNull String toString() {
            return switch (this) {
                case IDENT -> "identifier";
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
                case QUES -> "?";
                case KW_AXIOM -> "axiom";
                case KW_DEFUN -> "defun";
                case KW_DEFPROC -> "defproc";
                case KW_LET -> "let";
                case KW_CHECK -> "check";
            };
        }
    }

    public final Kind kind;
    public final String lexeme;

    public final String file;
    public final int line;
    public final int col;

    public Token(@NotNull Kind kind,
                 @NotNull String lexeme,
                 @NotNull String file,
                 int line,
                 int col) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.file = file;
        this.line = line;
        this.col = col;
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
    public @NotNull String toString() {
        return lexeme;
    }

    @Override
    public int hashCode() {
        if (this.kind == Kind.IDENT) {
            return Objects.hash(kind, lexeme);
        } else {
            // Token(COLON, "::") == Token(COLON, ":") 但 Token(COLON, ":") != ":"
            // 所以用第二个空字符串来区分 Token 和 String
            // 虽然按理说不会有人把 Token 和 String 装进同一个 HashMap
            return Objects.hash(kind, "");
        }
    }

    @TestOnly
    public static @NotNull Token ident(@NotNull String lexeme) {
        return new Token(Kind.IDENT, lexeme, "<test>", -1, -1);
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
            case QUES -> "?";
            case IDENT -> throw new IllegalArgumentException("IDENT token requires a lexeme");
            case KW_AXIOM -> "axiom";
            case KW_DEFUN -> "defun";
            case KW_DEFPROC -> "defproc";
            case KW_LET -> "let";
            case KW_CHECK -> "check";
        }, "<test>", -1, -1);
    }

    public static @NotNull ArrayList<Token> tokenize(@NotNull String file, @NotNull String input) {
        TokenizeContext ctx = new TokenizeContext(file);
        ctx.tokenize(input);
        return ctx.tokens;
    }

    private static class TokenizeContext {
        private final ArrayList<Token> tokens = new ArrayList<>();
        private final StringBuilder currentToken = new StringBuilder();
        private final String file;
        private int line = 1;
        private int col = 1;

        private TokenizeContext(String file) {
            this.file = file;
        }

        private void tokenize(String input) {
            char[] charArray = input.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                switch (c) {
                    case ' ', '\t', '\r', '\f' -> {
                        concludeToken();
                        col++;
                    }
                    case '\n' -> {
                        concludeToken();
                        line++;
                        col = 1;
                    }
                    case '(' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.LPAREN, "(", file, line, col));
                        col++;
                    }
                    case ')' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.RPAREN, ")", file, line, col));
                        col++;
                    }
                    case 'λ', '\\' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.LAMBDA, String.valueOf(c), file, line, col));
                        col++;
                    }
                    case '-' -> {
                        if (i + 1 < charArray.length) {
                            if (charArray[i + 1] == '>') {
                                concludeToken();
                                tokens.add(new Token(Kind.ARROW, "->", file, line, col));
                                i++;
                                col += 2;
                                continue;
                            } else if (charArray[i + 1] == '-') {
                                concludeToken();
                                i += 2;
                                col += 2;
                                while (i < charArray.length) {
                                    char cc = charArray[i];
                                    if (cc == '\n') {
                                        line++;
                                        col = 1;
                                        break;
                                    } else {
                                        i++;
                                        col++;
                                    }
                                }
                                continue;
                            }
                        }

                        currentToken.append(c);
                        col++;
                    }
                    case '→' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.ARROW, String.valueOf(c), file, line, col));
                        col++;
                    }
                    case '.' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.DOT, ".", file, line, col));
                        col++;
                    }
                    case ',' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.COMMA, ",", file, line, col));
                        col++;
                    }
                    case '=' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.EQ, "=", file, line, col));
                        col++;
                    }
                    case ':' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.COLON, ":", file, line, col));
                        col++;
                    }
                    case '*' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.ASTER, "*", file, line, col));
                        col++;
                    }
                    case '∀' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.PI, String.valueOf(c), file, line, col));
                        col++;
                    }
                    case '?' -> {
                        concludeToken();
                        tokens.add(new Token(Kind.QUES, "?", file, line, col));
                        col++;
                    }
                    default -> {
                        currentToken.append(c);
                        col++;
                    }
                }
            }
            concludeToken();
        }

        private void concludeToken() {
            if (currentToken.isEmpty()) {
                return;
            }
            String lexeme = currentToken.toString();
            if (KEYWORDS.containsKey(lexeme)) {
                tokens.add(new Token(KEYWORDS.get(lexeme),lexeme, file, line, col - lexeme.length()));
                currentToken.setLength(0);
                return;
            }

            tokens.add(new Token(Kind.IDENT, lexeme, file, line, col - lexeme.length()));
            currentToken.setLength(0);
        }

        private static final @NotNull HashMap<@NotNull String, @NotNull Kind> KEYWORDS;
        static {
            KEYWORDS = new HashMap<>();
            KEYWORDS.put("forall", Kind.PI);
            KEYWORDS.put("in", Kind.COLON);
            KEYWORDS.put("axiom", Kind.KW_AXIOM);
            KEYWORDS.put("postulate", Kind.KW_AXIOM);
            KEYWORDS.put("defun", Kind.KW_DEFUN);
            KEYWORDS.put("defproc", Kind.KW_DEFPROC);
            KEYWORDS.put("let", Kind.KW_LET);
            KEYWORDS.put("check", Kind.KW_CHECK);
        }
    }
}
