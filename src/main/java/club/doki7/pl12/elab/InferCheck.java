package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.exc.SourceRange;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.ParamGroup;
import club.doki7.pl12.syntax.Token;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class InferCheck {
    public static @NotNull Pair<Term, Type> infer(TypeContext ctx, Expr expr)
        throws TypeCheckException
    {
        return switch (expr) {
            case Expr.Var var -> inferVar(ctx, var);
            case Expr.Fun fun -> inferFun(ctx, fun);
            case Expr.Ann ann -> inferAnn(ctx, ann);
            case Expr.App app -> inferApp(ctx, app);
            case Expr.Arrow arrow -> inferArrow(ctx, arrow);
            case Expr.Pi pi -> inferPi(ctx, pi);
            case Expr.Hole hole -> inferHole(ctx, hole);
            case Expr.Lit lit -> inferLit(ctx, lit);
            case Expr.Paren paren -> infer(ctx, paren.expr());
            case Expr.PartialApp _ -> throw new UnsupportedOperationException("部分应用尚未实现");
            case Expr.Univ _ -> Pair.of(Term.UNIV, Type.UNIV);
        };
    }

    public static @NotNull Term check(TypeContext ctx, Expr expr, Type expectedType)
        throws TypeCheckException
    {
        Pair<Term, Type> inferred = infer(ctx, expr);
        Type inferredType = inferred.second();

        // TODO: implement unification
        if (!inferredType.equals(expectedType)) {
            throw new TypeCheckException(
                SourceRange.INVALID,
                "类型不匹配: 期望 " + expectedType + "，但推导出 " + inferredType
            );
        }

        return inferred.first();
    }

    public static @NotNull Pair<Term, Type> inferVar(TypeContext ctx, Expr.Var var)
        throws TypeCheckException
    {
        String varName = var.name().lexeme();
        Pair<Integer, Type> p = ctx.lookupLocal(varName);
        if (p != null) {
            int index = p.first();
            Type type = p.second();
            return Pair.of(new Term.Bound(index, varName), type);
        }

        Env.Entry entry = ctx.lookupGlobal(varName);
        if (entry != null) {
            return Pair.of(new Term.Free(new Name.Global(varName)), entry.type());
        }

        throw new TypeCheckException(var.name().range(), "未定义的局部或全局变量: " + varName);
    }

    public static @NotNull Pair<Term, Type> inferFun(TypeContext ctx, Expr.Fun fun)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("函数定义尚未实现");
    }

    public static @NotNull Pair<Term, Type> inferApp(TypeContext ctx, Expr.App app)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("函数应用尚未实现");
    }

    public static @NotNull Pair<Term, Type> inferAnn(TypeContext ctx, Expr.Ann ann)
        throws TypeCheckException
    {
        Term annTerm = check(ctx, ann.ann(), Type.UNIV);
        Type annType = Type.ofVal(ctx.eval(annTerm));
        Term exprTerm = check(ctx, ann.expr(), annType);
        return Pair.of(new Term.Ann(exprTerm, annTerm), annType);
    }

    public static @NotNull Pair<Term, Type> inferArrow(TypeContext ctx, Expr.Arrow arrow)
        throws TypeCheckException
    {
        return withDepthTracking(ctx, InferCheck::inferArrowImpl, arrow);
    }

    public static @NotNull Pair<Term, Type> inferPi(TypeContext ctx, Expr.Pi pi)
        throws TypeCheckException
    {
        return withDepthTracking(ctx, InferCheck::inferPiImpl, pi);
    }

    public static @NotNull Pair<Term, Type> inferHole(TypeContext ctx, Expr.Hole hole) {
        Term.Meta alpha = ctx.freshMetaAlpha(hole);
        Term.Meta tau = ctx.freshMetaTau(hole);
        Type type = Type.ofVal(ctx.eval(tau));
        return Pair.of(alpha, type);
    }

    public static @NotNull Pair<Term, Type> inferLit(TypeContext ctx, Expr.Lit lit)
        throws TypeCheckException
    {
        throw new UnsupportedOperationException("字面量类型推导尚未实现");
    }

    @FunctionalInterface
    private interface InferFn<E extends Expr> {
        @NotNull Pair<Term, Type> apply(TypeContext ctx, E e) throws TypeCheckException;
    }

    private static <E extends Expr>
    @NotNull Pair<Term, Type> withDepthTracking(TypeContext ctx, InferFn<E> fn, E expr)
        throws TypeCheckException
    {
        int depth = ctx.depth();
        try {
            return fn.apply(ctx, expr);
        } finally {
            ctx.restoreDepth(depth);
        }
    }

    private static @NotNull Pair<Term, Type> inferArrowImpl(TypeContext ctx, Expr.Arrow arrow)
        throws TypeCheckException
    {
        List<Expr> typeExprs = new ArrayList<>();
        typeExprs.add(arrow.from());
        Expr iter = arrow.to();
        while (iter instanceof Expr.Arrow(Expr from, Expr to, _)) {
            typeExprs.add(from);
            iter = to;
        }

        Term[] typeTerms = new Term[typeExprs.size()];
        for (int i = 0; i < typeExprs.size(); i++) {
            Term typeTerm = check(ctx, typeExprs.get(i), Type.UNIV);
            typeTerms[i] = typeTerm;
            ctx.bind();
        }

        Term resultType = check(ctx, iter, Type.UNIV);

        for (int i = typeExprs.size() - 1; i >= 0; i--) {
            resultType = new Term.Pi(null, typeTerms[i], resultType);
        }
        return Pair.of(resultType, Type.UNIV);
    }

    private static @NotNull Pair<Term, Type> inferPiImpl(TypeContext ctx, Expr.Pi pi)
        throws TypeCheckException
    {
        ParamGroup group = pi.paramGroup();
        ImmSeq<Token> names = group.names();
        Expr typeExpr = group.type();

        Term typeTerm = group.type() != null ? null : ctx.freshMeta(pi);

        Term[] typeTerms = new Term[names.size()];
        for (int i = 0; i < names.size(); i++) {
            if (typeExpr != null) {
                typeTerm = check(ctx, typeExpr, Type.UNIV);
            }
            typeTerms[i] = typeTerm;
            ctx.bind(names.get(i).lexeme(), Type.ofVal(ctx.eval(typeTerm)));
        }

        Term bodyTerm = check(ctx, pi.body(), Type.UNIV);

        for (int i = names.size() - 1; i >= 0; i--) {
            bodyTerm = new Term.Pi(names.get(i).lexeme(), typeTerms[i], bodyTerm);
        }
        return Pair.of(bodyTerm, Type.UNIV);
    }
}
