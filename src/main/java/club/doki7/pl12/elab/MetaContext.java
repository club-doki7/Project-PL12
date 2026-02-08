package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class MetaContext {
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
