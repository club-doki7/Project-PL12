package club.doki7.pl12.exc;

public final class LexicalException extends LocatedException {
    public LexicalException(SourceRange location, String message) {
        super(location, LexicalException.class, message);
    }
}
