package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class MetaContext {
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
        int metaId = sources.size();
        sources.add(source);
        solutions.add(null);
        return new Term.Meta(metaId, name);
    }

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

    private enum PendingReason {
        NOT_PATTERN("unification is not a pattern unification"),
        NEED_PRUNE("unification is pattern unification only after pruning");

        public final @NotNull String desc;

        PendingReason(@NotNull String desc) {
            this.desc = desc;
        }
    }

    private record Pending(@NotNull Object todo, @NotNull PendingReason reason) {}

    private final ArrayList<@NotNull MetaSource> sources = new ArrayList<>();
    private final ArrayList<@Nullable Term> solutions = new ArrayList<>();
    private final ArrayList<@NotNull Pending> pendings = new ArrayList<>();
}
