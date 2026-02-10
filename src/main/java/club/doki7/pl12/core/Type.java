package club.doki7.pl12.core;

import org.jetbrains.annotations.NotNull;

public record Type(Value value) {
    public static Type ofVal(Value value) {
        if (value == Value.Univ.UNIV) {
            return UNIV;
        }
        return new Type(value);
    }

    @Override
    public @NotNull String toString() {
        return value.toString();
    }

    public static final Type UNIV = new Type(Value.Univ.UNIV);
}
