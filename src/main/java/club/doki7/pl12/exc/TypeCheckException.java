package club.doki7.pl12.exc;

public sealed class TypeCheckException extends LocatedException permits UnificationException {
    public TypeCheckException(SourceRange location, String message) {
        super(location, TypeCheckException.class, message);
    }

    protected TypeCheckException(SourceRange location, Class<?> clazz, String message) {
        super(location, clazz, message);
    }
}
