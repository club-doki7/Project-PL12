package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

public record Pair<T1, T2>(T1 first, T2 second) {
    @Override
    public @NotNull String toString() {
        return "(" + first + ", " + second + ")";
    }

    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }
}
