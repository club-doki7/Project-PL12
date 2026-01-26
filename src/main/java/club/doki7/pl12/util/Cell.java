package club.doki7.pl12.util;

import club.doki7.pl12.ann.ObjectIdentity;
import org.jetbrains.annotations.NotNull;

@ObjectIdentity
public final class Cell<T> {
    public T value;

    public Cell(T value) {
        this.value = value;
    }

    public static <T> Cell<T> of(T value) {
        return new Cell<>(value);
    }

    @Override
    public @NotNull String toString() {
        return value.toString();
    }
}
