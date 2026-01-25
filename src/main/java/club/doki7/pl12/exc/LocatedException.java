package club.doki7.pl12.exc;

import club.doki7.pl12.syntax.Token;

public abstract class LocatedException extends Exception {
    public LocatedException(Token location, Class<?> clazz, String message) {
        super("At " + location.file + ":" + location.line + ": "
              + clazz.getSimpleName()
              + ": " + message);
        this.location = location;
        this.message = message;
    }

    public final Token location;
    public final String message;
}
