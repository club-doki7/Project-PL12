package club.doki7.pl12.exc;

import club.doki7.pl12.syntax.Token;

import java.util.ArrayList;
import java.util.List;

public abstract class LocatedException extends Exception {
    public record LocatedMessage(String message, Token location) {}

    public final Token location;
    public final String message;
    public final List<LocatedMessage> trace = new ArrayList<>();

    public LocatedException(Token location, Class<?> clazz, String message) {
        super(location.file + ":" + location.line + ":" + location.col + ": "
              + clazz.getSimpleName()
              + ": " + message);
        this.location = location;
        this.message = message;
    }
}
