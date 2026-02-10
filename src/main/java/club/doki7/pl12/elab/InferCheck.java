package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.ParamGroup;
import club.doki7.pl12.syntax.Token;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;

public final class InferCheck {
    public static @NotNull Pair<Term, Type> infer(Context ctx, Expr expr)
        throws TypeCheckException
    {
        return switch (expr) {
            case Expr.Var(Token name) -> {
                String varName = name.lexeme();
                Integer index = DBI.find(ctx.dbiEnv(), varName);
                if (index != null) {
                    Type type = DBI.get(ctx.types(), index);
                    yield Pair.of(new Term.Bound(index, varName), type);
                }

                Env.Entry entry = ctx.env().lookup(varName);
                if (entry != null) {
                    yield Pair.of(new Term.Free(new Name.Global(varName)), entry.type());
                }

                throw new TypeCheckException(name.range(), "未定义的局部或全局变量: " + varName);
            }
            case Expr.Fun(ImmSeq<ParamGroup> paramGroups, Expr body, Token fun, Token arrow) -> {
                throw new UnsupportedOperationException("函数定义尚未实现");
            }
            case Expr.Ann ann -> null;
            case Expr.App app -> null;
            case Expr.Arrow arrow -> null;
            case Expr.Hole hole -> null;
            case Expr.Lit lit -> null;
            case Expr.Paren paren -> null;
            case Expr.PartialApp partialApp -> null;
            case Expr.Pi pi -> null;
            case Expr.Univ univ -> null;
        };
    }

    public static @NotNull Term check(Context ctx, Expr expr, Type expectedType)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("类型检查尚未实现");
    }
}
