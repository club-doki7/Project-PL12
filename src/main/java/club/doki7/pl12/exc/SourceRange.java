package club.doki7.pl12.exc;

public record SourceRange(SourceLocation start, SourceLocation end) {
    public SourceRange {
        assert start.file().equals(end.file());
    }
}
