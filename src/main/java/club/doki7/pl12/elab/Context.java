package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Context
{
    public int depth() {
        return localEnv.size();
    }

    public void bind() {
        localEnv.add(null);
        types.add(null);
    }

    public void bind(String newName, Type newType) {
        localEnv.add(newName);
        types.add(newType);
    }

    public void bind(List<String> newNames, List<Type> newTypes) {
        assert newNames.size() == newTypes.size();
        localEnv.addAll(newNames);
        types.addAll(newTypes);
    }

    public void unbind() {
        localEnv.removeLast();
        types.removeLast();
    }

    public void unbind(int n) {
        for (int i = 0; i < n; i++) {
            localEnv.removeLast();
            types.removeLast();
        }
    }

    public void restoreDepth(int depth) {
        int currentDepth = depth();
        assert currentDepth >= depth;
        if (currentDepth == depth) {
            return;
        }
        unbind(currentDepth - depth);
    }

    public @Nullable Pair<@NotNull Integer, @NotNull Type> lookupLocal(String name) {
        int index = localEnv.lastIndexOf(name);
        if (index == -1) {
            return null;
        }

        Type type = Objects.requireNonNull(types.get(index));
        return new Pair<>(localEnv.size() - 1 - index, type);
    }

    public @Nullable Env.Entry lookupGlobal(String name) {
        return env.lookup(name);
    }

    public Value eval(@NotNull Term term) {
        return eval.eval(term);
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

        record PiParamType(@NotNull Expr.Pi pi) implements MetaSource {}

        record Hole(@NotNull Expr.Hole hole) implements MetaSource {}

        record HoleType(@NotNull Expr.Hole hole) implements MetaSource {}
    }

    public int metaCount() {
        return metaSources.size();
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

    public Term.Meta freshMeta(@NotNull Expr.Pi pi) {
        MetaSource source = new MetaSource.PiParamType(pi);
        String name = pi.paramGroup().names().getFirst().lexeme();
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

    public static @NotNull Context make(@NotNull Env env) {
        return new Context(env);
    }

    private Term.Meta freshMeta(@NotNull MetaSource source, @NotNull String name) {
        int metaId = metaCount();
        metaSources.add(source);
        metaSolutions.add(null);
        return new Term.Meta(metaId, name);
    }

    private Context(@NotNull Env env) {
        this.env = env;
        this.eval = Eval.make(env);
    }

    private final @NotNull Env env;
    private final @NotNull Eval eval;
    private final @NotNull ArrayList<@Nullable String> localEnv = new ArrayList<>();
    private final @NotNull ArrayList<@Nullable Type> types = new ArrayList<>();
    private final @NotNull ArrayList<@NotNull MetaSource> metaSources = new ArrayList<>();
    private final @NotNull ArrayList<@Nullable Term> metaSolutions = new ArrayList<>();
}
