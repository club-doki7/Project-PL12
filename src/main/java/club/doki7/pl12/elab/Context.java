package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record Context(Env env,
                      SnocList<ImmSeq<String>> localEnv,
                      SnocList<ImmSeq<Type>> types,
                      MetaContext mctx)
{
    public record MetaContext(ArrayList<@NotNull MetaSource> sources,
                              ArrayList<@Nullable Term> solutions) {}

    public sealed interface MetaSource {
        record DefParamType(@NotNull Command.Definition def,
                            int paramGroupIndex,
                            int paramIndex)
            implements MetaSource {}

        record FunParamType(@NotNull Expr.Fun fun,
                            int paramGroupIndex,
                            int paramIndex)
            implements MetaSource {}

        record Hole(@NotNull Expr.Hole hole) implements MetaSource {}

        record HoleType(@NotNull Expr.Hole hole) implements MetaSource {}
    }

    public Context bind(ImmSeq<String> names, ImmSeq<Type> types) {
        assert names.size() == types.size();
        return new Context(env,
                           SnocList.snoc(localEnv, names),
                           SnocList.snoc(this.types, types),
                           mctx);
    }

    public Term.Meta freshMeta(@NotNull Command.Definition def,
                               int paramGroupIndex,
                               int paramIndex) {
        MetaSource source = new MetaSource.DefParamType(def, paramGroupIndex, paramIndex);
        String name = def.paramGroups().get(paramGroupIndex).names().get(paramIndex).lexeme();
        return freshMeta(source, name);
    }

    public Term.Meta freshMeta(@NotNull Expr.Fun fun,
                               int paramGroupIndex,
                               int paramIndex) {
        MetaSource source = new MetaSource.FunParamType(fun, paramGroupIndex, paramIndex);
        String name = fun.paramGroups().get(paramGroupIndex).names().get(paramIndex).lexeme();
        return freshMeta(source, name);
    }

    public Term.Meta freshMetaAlpha(@NotNull Expr.Hole hole) {
        MetaSource source = new MetaSource.Hole(hole);
        return freshMeta(source, "α");
    }

    public Term.Meta freshMetaTau(@NotNull Expr.Hole hole) {
        MetaSource source = new MetaSource.HoleType(hole);
        return freshMeta(source, "τ");
    }

    private Term.Meta freshMeta(@NotNull MetaSource source, @NotNull String name) {
        int metaId = mctx.sources.size();
        mctx.sources.add(source);
        mctx.solutions.add(null);
        return new Term.Meta(metaId, name);
    }
}
