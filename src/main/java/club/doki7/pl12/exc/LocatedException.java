package club.doki7.pl12.exc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class LocatedException extends Exception {
    public record LocatedMessage(@Nullable SourceRange location, @NotNull String message) {}

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

    public void addTrace(SourceRange location, String message) {
        trace.add(new LocatedMessage(location, message));
    }

    public void addTrace(String message) {
        trace.add(new LocatedMessage(null, message));
    }
}
