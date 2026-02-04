package club.doki7.pl12.exc;

public final class ParseException extends LocatedException {
    public ParseException(SourceRange location, String message) {
        super(location, ParseException.class, message);
    }
}
