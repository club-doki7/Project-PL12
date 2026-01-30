package club.doki7.pl12.syntax;

import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.UV;

import java.util.HashMap;
import java.util.HashSet;

public record ParseContext(char[] buf,
                           int position,
                           String file,
                           int line,
                           int col,
                           UV<HashMap<String, Operator.Infix>> infixOps,
                           UV<HashSet<String>> mixfixFragments)
{
    public Pair<Token, ParseContext> nextToken() {
        ParseContext ctx = skipWhitespaceAndComments();

        if (ctx.position >= ctx.buf.length) {
            return Pair.of(new Token(Token.Kind.EOI, "", ctx.file, ctx.line, ctx.col), ctx);
        }

        char c = ctx.buf[ctx.position];
        int startLine = ctx.line;
        int startCol = ctx.col;

        // 单字符 token
        return switch (c) {
            case '(' -> Pair.of(
                    new Token(Token.Kind.LPAREN, "(", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case ')' -> Pair.of(
                    new Token(Token.Kind.RPAREN, ")", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '.' -> Pair.of(
                    new Token(Token.Kind.DOT, ".", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case ',' -> Pair.of(
                    new Token(Token.Kind.COMMA, ",", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '*' -> Pair.of(
                    new Token(Token.Kind.ASTER, "*", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case ':' -> Pair.of(
                    new Token(Token.Kind.COLON, ":", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '=' -> Pair.of(
                    new Token(Token.Kind.EQ, "=", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case 'λ' -> Pair.of(
                    new Token(Token.Kind.LAMBDA, "λ", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '\\' -> Pair.of(
                    new Token(Token.Kind.LAMBDA, "\\", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '→' -> Pair.of(
                    new Token(Token.Kind.ARROW, "→", ctx.file, startLine, startCol),
                    ctx.advance(1)
            );
            case '∀', 'Π' -> Pair.of(
                    new Token(Token.Kind.PI, String.valueOf(c), ctx.file, startLine, startCol),
                    ctx.advance(1)
            );

            // 多字符 token
            case '-' -> ctx.tryArrow(startLine, startCol);
            case '?' -> ctx.tryDoubleQuestion(startLine, startCol);

            default -> {
                if (isIdentStart(c)) {
                    yield ctx.readIdentOrKeyword(startLine, startCol);
                } else {
                    // 尝试作为运算符处理
                    yield ctx.readOperator(startLine, startCol);
                }
            }
        };
    }

    /// 跳过空白字符和注释
    private ParseContext skipWhitespaceAndComments() {
        ParseContext ctx = this;
        while (ctx.position < ctx.buf.length) {
            char c = ctx.buf[ctx.position];

            if (c == '\n') {
                ctx = new ParseContext(ctx.buf, ctx.position + 1, ctx.file,
                        ctx.line + 1, 1, ctx.infixOps, ctx.mixfixFragments);
            } else if (Character.isWhitespace(c)) {
                ctx = ctx.advance(1);
            } else if (c == '-' && ctx.position + 1 < ctx.buf.length && ctx.buf[ctx.position + 1] == '-') {
                // 单行注释 --
                ctx = ctx.advance(2);
                while (ctx.position < ctx.buf.length && ctx.buf[ctx.position] != '\n') {
                    ctx = ctx.advance(1);
                }
            } else if (c == '{' && ctx.position + 1 < ctx.buf.length && ctx.buf[ctx.position + 1] == '-') {
                // 块注释 {- ... -}
                ctx = ctx.advance(2);
                int depth = 1;
                while (ctx.position < ctx.buf.length && depth > 0) {
                    if (ctx.buf[ctx.position] == '\n') {
                        ctx = new ParseContext(ctx.buf, ctx.position + 1, ctx.file,
                                ctx.line + 1, 1, ctx.infixOps, ctx.mixfixFragments);
                    } else if (ctx.buf[ctx.position] == '{' &&
                               ctx.position + 1 < ctx.buf.length &&
                               ctx.buf[ctx.position + 1] == '-') {
                        depth++;
                        ctx = ctx.advance(2);
                    } else if (ctx.buf[ctx.position] == '-' &&
                               ctx.position + 1 < ctx.buf.length &&
                               ctx.buf[ctx.position + 1] == '}') {
                        depth--;
                        ctx = ctx.advance(2);
                    } else {
                        ctx = ctx.advance(1);
                    }
                }
            } else {
                break;
            }
        }
        return ctx;
    }

    /// 前进 n 个字符
    private ParseContext advance(int n) {
        return new ParseContext(buf, position + n, file, line, col + n, infixOps, mixfixFragments);
    }

    /// 尝试解析 `->`
    private Pair<Token, ParseContext> tryArrow(int startLine, int startCol) {
        if (position + 1 < buf.length && buf[position + 1] == '>') {
            return Pair.of(
                    new Token(Token.Kind.ARROW, "->", file, startLine, startCol),
                    advance(2)
            );
        }
        // 单独的 `-` 可能是运算符的一部分
        return readOperator(startLine, startCol);
    }

    /// 尝试解析 `??`
    private Pair<Token, ParseContext> tryDoubleQuestion(int startLine, int startCol) {
        if (position + 1 < buf.length && buf[position + 1] == '?') {
            return Pair.of(
                    new Token(Token.Kind.DQUES, "??", file, startLine, startCol),
                    advance(2)
            );
        }
        // 单独的 `?` 可能是标识符的一部分
        return readIdentOrKeyword(startLine, startCol);
    }

    /// 判断字符是否可以作为标识符开头
    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '?';
    }

    /// 判断字符是否可以作为标识符的一部分
    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '\'' || c == '?';
    }

    /// 读取标识符或关键字
    private Pair<Token, ParseContext> readIdentOrKeyword(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        ParseContext ctx = this;

        while (ctx.position < ctx.buf.length && isIdentPart(ctx.buf[ctx.position])) {
            sb.append(ctx.buf[ctx.position]);
            ctx = ctx.advance(1);
        }

        String lexeme = sb.toString();
        Token.Kind kind = switch (lexeme) {
            case "forall" -> Token.Kind.PI;
            case "axiom" -> Token.Kind.KW_AXIOM;
            case "defun" -> Token.Kind.KW_DEFUN;
            case "defproc" -> Token.Kind.KW_DEFPROC;
            case "let" -> Token.Kind.KW_LET;
            case "check" -> Token.Kind.KW_CHECK;
            default -> {
                // 检查是否是混缀运算符片段
                if (mixfixFragments.value.contains(lexeme)) {
                    yield Token.Kind.MIXFIX_FRAG;
                }
                yield Token.Kind.IDENT;
            }
        };

        return Pair.of(new Token(kind, lexeme, file, startLine, startCol), ctx);
    }

    /// 读取运算符
    private Pair<Token, ParseContext> readOperator(int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        ParseContext ctx = this;

        while (ctx.position < ctx.buf.length && isOperatorChar(ctx.buf[ctx.position])) {
            sb.append(ctx.buf[ctx.position]);
            ctx = ctx.advance(1);
        }

        String lexeme = sb.toString();
        if (lexeme.isEmpty()) {
            // 无法识别的字符，返回单个字符作为标识符
            lexeme = String.valueOf(buf[position]);
            ctx = advance(1);
            return Pair.of(new Token(Token.Kind.IDENT, lexeme, file, startLine, startCol), ctx);
        }

        // 检查是否是中缀运算符
        if (infixOps.value.containsKey(lexeme)) {
            return Pair.of(new Token(Token.Kind.BINARY, lexeme, file, startLine, startCol), ctx);
        }

        // 检查是否是混缀运算符片段
        if (mixfixFragments.value.contains(lexeme)) {
            return Pair.of(new Token(Token.Kind.MIXFIX_FRAG, lexeme, file, startLine, startCol), ctx);
        }

        // 默认作为标识符
        return Pair.of(new Token(Token.Kind.IDENT, lexeme, file, startLine, startCol), ctx);
    }

    /// 判断字符是否可以作为运算符的一部分
    private static boolean isOperatorChar(char c) {
        return "!#$%&*+-/<=>@^|~".indexOf(c) >= 0;
    }

    /// 创建初始解析上下文
    public static ParseContext of(String source, String file) {
        return new ParseContext(
                source.toCharArray(),
                0,
                file,
                1,
                1,
                new UV<>(new HashMap<>()),
                new UV<>(new HashSet<>())
        );
    }

    /// 创建初始解析上下文（带有预定义的运算符）
    public static ParseContext of(String source,
                                  String file,
                                  HashMap<String, Operator.Infix> infixOps,
                                  HashSet<String> mixfixFragments) {
        return new ParseContext(
                source.toCharArray(),
                0,
                file,
                1,
                1,
                new UV<>(infixOps),
                new UV<>(mixfixFragments)
        );
    }
}
