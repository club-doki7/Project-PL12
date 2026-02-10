package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.Seq;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record Context(Env env,
                      SnocList<Seq<String>> localEnv,
                      SnocList<Seq<Type>> types,
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

    public Context bind(Seq<String> names, Seq<Type> types) {
        assert names.getClass() == types.getClass() && names.size() == types.size();
        return new Context(env,
                           SnocList.snoc(localEnv, names),
                           SnocList.snoc(this.types, types),
                           mctx);
    }

    public Pair<Integer, Type> lookupLocal(String name) {
        int acc = 0;
        SnocList<Seq<String>> localEnvIter = localEnv;
        SnocList<Seq<Type>> typesIter = types;
        while (localEnvIter instanceof SnocList.Snoc(SnocList<Seq<String>> init,
                                                     Seq<String> last, _)
               && typesIter instanceof SnocList.Snoc(SnocList<Seq<Type>> typesInit,
                                                     Seq<Type> typesLast, _)) {
            assert last.size() == typesLast.size();
            if (last instanceof ImmSeq<String> lastImmSeq &&
                typesLast instanceof ImmSeq<Type> typesLastImmSeq) {
                for (int i = last.size() - 1; i >= 0; i--) {
                    if (lastImmSeq.get(i).equals(name)) {
                        return new Pair<>(acc + (last.size() - 1 - i), typesLastImmSeq.get(i));
                    }
                }
            } else {
                assert !(last instanceof ImmSeq) && !(typesLast instanceof ImmSeq);
            }

            acc += last.size();
            localEnvIter = init;
            typesIter = typesInit;
        }

        assert localEnvIter instanceof SnocList.Nil && typesIter instanceof SnocList.Nil;
        return null;
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

    public int metaCount() {
        return mctx.sources.size();
    }

    private Term.Meta freshMeta(@NotNull MetaSource source, @NotNull String name) {
        int metaId = metaCount();
        mctx.sources.add(source);
        mctx.solutions.add(null);
        return new Term.Meta(metaId, name);
    }
}
