package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public final class UV<T> {
    public final @NotNull T e;

    public UV(@NotNull T e) {
        this.e = e;
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
        return e.toString();
    }
}
