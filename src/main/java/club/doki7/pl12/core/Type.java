package club.doki7.pl12.core;

import org.jetbrains.annotations.NotNull;

public record Type(Value value) {
    public static Type ofVal(Value value) {
        return new Type(value);
    }

    @Override
    public @NotNull String toString() {
        return value.toString();
    }
}
