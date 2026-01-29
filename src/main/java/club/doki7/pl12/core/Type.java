package club.doki7.pl12.core;

import org.jetbrains.annotations.NotNull;

public record Type(Value value) {
    public enum Totality {
        TOTAL,
        PARTIAL;

        public Totality add(Totality other) {
            if (this == PARTIAL || other == PARTIAL) {
                return PARTIAL;
            } else {
                return TOTAL;
            }
        }

        @Override
        public @NotNull String toString() {
            return switch (this) {
                case TOTAL -> "⊤";
                case PARTIAL -> "⊥";
            };
        }
    }

//    public static Type ofVal(Value value, Totality totality) {
//        return new Type(value, totality);
//    }

    public static Type ofVal(Value value) {
        return new Type(value);
    }

    @Override
    public @NotNull String toString() {
        return value.toString();
    }
}
