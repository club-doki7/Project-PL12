package club.doki7.pl12.core;

import club.doki7.pl12.util.ImmSeq;
import club.doki7.pl12.util.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Objects;

public sealed interface Term {
    record Ann(@NotNull Term term, @NotNull Term type) implements Term {}

    record Bound(int index, @NotNull String name) implements Term, Value.RigidHead {
        @Override
        public @NotNull String toString() {
            return TextUtil.subscriptNum(name, index);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Bound(int otherIndex, _))) return false;
            return index == otherIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Bound.class, index);
        }
    }

    record Free(@NotNull Name name) implements Term, Value.RigidHead {}

    record Lam(ImmSeq<String> paramNames, @NotNull Term body) implements Term {
        public static @NotNull Lam of(ImmSeq<String> paramNames, @NotNull Term body) {
            return new Lam(paramNames, body);
        }
    }

    final class Univ implements Term {
        private static final @NotNull Univ UNIV = new Univ();

        private Univ() {}

        @Override
        public @NotNull String toString() {
            return "type";
        }
    }

    record Pi(@Nullable String pramName,
              @NotNull Term type,
              @NotNull Term body)
        implements Term {}

    record App(@NotNull Term func, @NotNull ImmSeq<Term> args) implements Term {
        @TestOnly
        public App(@NotNull Term func, @NotNull Term... args) {
            this(func, ImmSeq.of(args));
        }
    }

    record Meta(int id, @NotNull String name) implements Term {
        @Override
        public @NotNull String toString() {
            return TextUtil.subscriptNum("?" + name, id);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Meta(int otherId, _))) return false;
            return id == otherId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Meta.class, id);
        }
    }

    record SolvedMeta(int id, @NotNull Term solution) implements Term {}

    public static final @NotNull Univ UNIV = Univ.UNIV;
}
