package club.doki7.pl12.exc;

public record SourceRange(Location start, Location end) {
    public SourceRange {
        assert start.file().equals(end.file());
    }
}
