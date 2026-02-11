package club.doki7.pl12.core;

import club.doki7.pl12.util.TextUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed interface Name {
    record Global(@NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    record Local(int level, @NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return TextUtil.superscriptNum("𝓛" + name, level);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Local(int otherIndex, _))) return false;
            return level == otherIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Local.class, level);
        }
    }

    record Quote(int level, @NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return TextUtil.superscriptNum("𝓠" + name, level);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Quote(int otherIndex, _))) return false;
            return level == otherIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Quote.class, level);
        }
    }
}
