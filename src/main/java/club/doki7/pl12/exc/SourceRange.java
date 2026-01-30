package club.doki7.pl12.exc;

import org.jetbrains.annotations.NotNull;

public record SourceRange(@NotNull SourceLocation start, @NotNull SourceLocation end) {
    public SourceRange {
        assert start.file().equals(end.file());
    }

    public boolean invalid() {
        return start.invalid() || end.invalid();
    }
}
