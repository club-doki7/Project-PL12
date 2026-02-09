package club.doki7.pl12.core;

import club.doki7.pl12.util.TextUtil;
import org.jetbrains.annotations.NotNull;

public sealed interface Name {
    record Global(@NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    record Local(int index, @NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return TextUtil.superscriptNum(name, index);
        }
    }

    record Quote(int index, @NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return TextUtil.superscriptNum("𝒬〈" + name + "〉", index);
        }
    }
}
