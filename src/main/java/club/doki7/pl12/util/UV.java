package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public final class UV<T> {
    public final @NotNull T v;

    public UV(@NotNull T v) {
        this.v = v;
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return o != null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return v.toString();
    }
}
