package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.TextUtil;
import club.doki7.pl12.util.VoidSeq;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class InferCheck {
    public static @NotNull Pair<Term, Type> infer(Context ctx, Expr expr)
        throws TypeCheckException
    {
        return switch (expr) {
            case Expr.Var var -> inferVar(ctx, var);
            case Expr.Fun fun -> inferFun(ctx, fun);
            case Expr.Ann ann -> inferAnn(ctx, ann);
            case Expr.App app -> inferApp(ctx, app);
            case Expr.Arrow arrow -> inferArrow(ctx, arrow);
            case Expr.Pi pi -> inferPi(ctx, pi);
            case Expr.Hole hole -> null;
            case Expr.Lit lit -> null;
            case Expr.Paren paren -> null;
            case Expr.PartialApp partialApp -> null;
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
        Pair<Integer, Type> p = ctx.lookupLocal(varName);
        if (p != null) {
            int index = p.first();
            Type type = p.second();
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

    public static @NotNull Pair<Term, Type> inferArrow(Context ctx, Expr.Arrow arrow)
        throws TypeCheckException
    {
        // 假设存在这样一个类型：
        //   forall (a b c : type), a -> b -> c -> nat
        //                          ^~~~~~~~~~~
        // 在检查类型 a -> (b -> (c -> nat)) 时，由于 a, b 和 c 上没有 forall，它们不会向上下文中引入新的绑定
        // 因此，a, b, c 和 nat 的检查事实上都可以在原有的 context 上直接进行

        // 首先，尽可能多地收集箭头类型
        List<Expr> typeExprs = new ArrayList<>();
        typeExprs.add(arrow.from());
        Expr iter = arrow.to();
        while (iter instanceof Expr.Arrow(Expr from, Expr to, _)) {
            typeExprs.add(from);
            iter = to;
        }

        // 然后，对这部分箭头类型进行检查
        Term[] typeTerms = new Term[typeExprs.size()];

        // 这里还有个问题，考虑以下类型：
        //   forall (a : type), a -> a -> a -> a
        // 考虑德布鲁因索引，上述类型应该正规化为
        //   forall (a : type), a₀ -> a₁ -> a₂ -> a₃
        // 但如果我们真的用同一个 Context 来作检查，那么在检查过程中，四个 a 会被赋予同一个德布鲁因索引
        // 这显然是错误的，所以我们面临问题：不需要真的向 Context 中插入绑定，但又需要在检查过程中区分不同的 a
        // 解决方案就是 VoidSeq
        VoidSeq<String> namesSeq = new VoidSeq<>(0);
        VoidSeq<Type> typesSeq = new VoidSeq<>(0);
        ctx = ctx.bind(namesSeq, typesSeq);
        for (int i = 0; i < typeExprs.size(); i++) {
            Term typeTerm = check(ctx, typeExprs.get(i), Type.UNIV);
            typeTerms[i] = typeTerm;

            // 修改 namesSeq/typesSeq 的大小来“撑开” Context
            namesSeq.size += 1;
            typesSeq.size += 1;
        }

        // 最后，检查箭头类型的结果类型
        Term resultType = check(ctx, iter, Type.UNIV);

        // 从后往前构造出 Pi 类型
        for (int i = typeExprs.size() - 1; i >= 0; i--) {
            resultType = new Term.Pi(null, typeTerms[i], resultType);
        }
        return Pair.of(resultType, Type.UNIV);
    }

    public static @NotNull Pair<Term, Type> inferPi(Context ctx, Expr.Pi pi)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("Pi 类型尚未实现");
    }
}
