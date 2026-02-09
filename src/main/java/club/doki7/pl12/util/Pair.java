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

    public static <T1, T2, T1Sub extends T1, T2Sub extends T2>
    Pair<T1, T2> upcast(Pair<T1Sub, T2Sub> p) {
        return new Pair<>(p.first(), p.second());
    }
}
