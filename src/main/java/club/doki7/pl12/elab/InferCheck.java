package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Argument;
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
                Integer index = DBI.find(ctx.localEnv(), varName);
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
            case Expr.Ann(Expr expr1, Expr ann, _) -> {
                Term annTerm = check(ctx, ann, Type.UNIV);
                Type annType = Type.ofVal(Eval.make(ctx.env()).eval(annTerm));
                Term exprTerm = check(ctx, expr1, annType);
                yield Pair.of(new Term.Ann(exprTerm, annTerm), annType);
            }
            case Expr.App(Expr func, ImmSeq<Argument> args, _) -> {
                Pair<Term, Type> funcP = infer(ctx, func);

                throw new UnsupportedOperationException("函数应用尚未实现");
            }
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
