package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;

public final class InferCheck {
    public static @NotNull Pair<Term, Type> infer(Context ctx, Expr expr)
        throws TypeCheckException
    {
        return switch (expr) {
            case Expr.Var var -> inferVar(ctx, var);
            case Expr.Fun fun -> inferFun(ctx, fun);
            case Expr.Ann ann -> inferAnn(ctx, ann);
            case Expr.App app -> inferApp(ctx, app);
            case Expr.Arrow arrow -> null;
            case Expr.Hole hole -> null;
            case Expr.Lit lit -> null;
            case Expr.Paren paren -> null;
            case Expr.PartialApp partialApp -> null;
            case Expr.Pi pi -> null;
            case Expr.Univ _ -> Pair.of(Term.UNIV, Type.UNIV);
        };
    }

    public static @NotNull Term check(Context ctx, Expr expr, Type expectedType)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("类型检查尚未实现");
    }

    public static @NotNull Pair<Term, Type> inferVar(Context ctx, Expr.Var var)
        throws TypeCheckException
    {
        String varName = var.name().lexeme();
        Integer index = DBI.find(ctx.localEnv(), varName);
        if (index != null) {
            Type type = DBI.get(ctx.types(), index);
            return Pair.of(new Term.Bound(index, varName), type);
        }

        Env.Entry entry = ctx.env().lookup(varName);
        if (entry != null) {
            return Pair.of(new Term.Free(new Name.Global(varName)), entry.type());
        }

        throw new TypeCheckException(var.name().range(), "未定义的局部或全局变量: " + varName);
    }

    public static @NotNull Pair<Term, Type> inferFun(Context ctx, Expr.Fun fun)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("函数定义尚未实现");
    }

    public static @NotNull Pair<Term, Type> inferApp(Context ctx, Expr.App app)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("函数应用尚未实现");
    }

    public static @NotNull Pair<Term, Type> inferAnn(Context ctx, Expr.Ann ann)
        throws TypeCheckException
    {
        Term annTerm = check(ctx, ann.ann(), Type.UNIV);
        Type annType = Type.ofVal(Eval.make(ctx.env()).eval(annTerm));
        Term exprTerm = check(ctx, ann.expr(), annType);
        return Pair.of(new Term.Ann(exprTerm, annTerm), annType);
    }
}
