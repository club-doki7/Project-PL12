package club.doki7.pl12.exc;

import org.jetbrains.annotations.NotNull;

public record SourceLocation(@NotNull String file, int pos, int line, int col) {
    public boolean invalid() {
        return line <= 0 || col <= 0;
    }

    public static @NotNull SourceLocation of(@NotNull String file, int pos, int line, int col) {
        return new SourceLocation(file, pos, line, col);
    }
}
