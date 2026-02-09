package club.doki7.pl12.exc;

public sealed class ParseException extends LocatedException permits LexicalException {
    public ParseException(SourceRange location, String message) {
        super(location, ParseException.class, message);
    }

    protected ParseException(SourceRange location, Class<?> clazz, String message) {
        super(location, clazz, message);
    }
}
