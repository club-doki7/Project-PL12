package club.doki7.pl12.core;

import club.doki7.pl12.util.CommonUtil;
import club.doki7.pl12.util.UV;
import org.jetbrains.annotations.NotNull;

public sealed interface Name {
    record Global(@NotNull String name) implements Name {
        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    record Local(int index, @NotNull UV<String> name) implements Name {
        @Override
        public @NotNull String toString() {
            return CommonUtil.superscriptNum(name.e, index);
        }
    }

    record Quote(int index) implements Name {
        @Override
        public @NotNull String toString() {
            return CommonUtil.superscriptNum("\uD835\uDCAC", index);
        }
    }
}
