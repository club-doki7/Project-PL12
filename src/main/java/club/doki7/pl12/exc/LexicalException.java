package club.doki7.pl12.exc;

public final class LexicalException extends ParseException {
    public LexicalException(SourceRange location, String message) {
        super(location, LexicalException.class, message);
    }
}
