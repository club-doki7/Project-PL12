package club.doki7.pl12.exc;

import org.jetbrains.annotations.NotNull;

public record SourceRange(@NotNull SourceLocation start, @NotNull SourceLocation end) {
    public SourceRange {
        assert start.file().equals(end.file());
    }

    public boolean invalid() {
        return start.invalid() || end.invalid();
    }

    public static @NotNull SourceRange of(@NotNull SourceLocation start,
                                          @NotNull SourceLocation end) {
        return new SourceRange(start, end);
    }

    public static @NotNull SourceRange of(@NotNull String file, int pos, int line, int col) {
        SourceLocation loc = SourceLocation.of(file, pos, line, col);
        return new SourceRange(loc, loc);
    }

    public static final @NotNull SourceRange INVALID =
        new SourceRange(SourceLocation.INVALID, SourceLocation.INVALID);
}
