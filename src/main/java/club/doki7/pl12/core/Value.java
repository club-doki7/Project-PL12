package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import org.jetbrains.annotations.NotNull;

public sealed interface Value {
    @NotNull Term term();

    record Flex(@NotNull Term.Meta meta,
                @NotNull ConsRevList<@NotNull Value> spine,
                @NotNull Term term)
            implements Value
    {
        public Flex(@NotNull Term.Meta meta) {
            this(meta, ConsRevList.nil(), meta);
        }
    }

    record Rigid(@NotNull Name name,
                 @NotNull ConsRevList<@NotNull Value> spine,
                 @NotNull Term term)
            implements Value
    {
        public Rigid(@NotNull Name name, @NotNull Term term) {
            this(name, ConsRevList.nil(), term);
        }
    }

    record Lam(@NotNull Env env,
               @NotNull ConsRevList<@NotNull Value> localEnv,
               @NotNull Term body,
               @NotNull Term term)
            implements Value
    {
        public Lam {
            assert term instanceof Term.LamChk || term instanceof Term.LamInf;
        }
    }

    record Pi(@NotNull Env env,
              @NotNull Type paramType,
              @NotNull ConsRevList<@NotNull Value> localEnv,
              @NotNull Term returnType,
              @NotNull Term.Pi term)
            implements Value
    {}

    record Univ(@NotNull Term.Univ term) implements Value {}
}
