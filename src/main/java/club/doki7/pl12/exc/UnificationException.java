package club.doki7.pl12.exc;

public final class UnificationException extends LocatedException {
    public UnificationException(SourceRange location, String message) {
        super(location, UnificationException.class, message);
    }
}
