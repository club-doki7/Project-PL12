package club.doki7.pl12.exc;

import java.util.ArrayList;
import java.util.List;

public abstract class LocatedException extends Exception {
    public record LocatedMessage(SourceRange location, String message) {}

    public final SourceRange location;
    public final String message;
    public final List<LocatedMessage> trace = new ArrayList<>();

    public LocatedException(SourceRange location, Class<?> clazz, String message) {
        super(location.start().file()
              + ":" + location.start().line()
              + ":" + location.start().col()
              + ": " + clazz.getSimpleName()
              + ": " + message);
        this.location = location;
        this.message = message;
    }
}
