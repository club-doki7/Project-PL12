package club.doki7.pl12.util;

import club.doki7.pl12.ann.ObjectIdentity;
import org.jetbrains.annotations.NotNull;

/// A mutable container for a value of type T.
///
/// This class is useful when you need to have a mutable reference to a value,
/// for example, when implementing algorithms that require mutable state.
///
/// Note that since this class is considered immutable in contexts requiring it
/// to be immutable (e.g., as fields in `record`s), it uses object identity for
/// equality and hash code calculations.
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
