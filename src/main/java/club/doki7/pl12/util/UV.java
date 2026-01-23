package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

/// ## UV: Universal Vertex
///
/// a type whose inhabitants are equal to anything, and all have the same hash
/// code.
///
/// When you want to exclude several certain fields from equality and hash code calculation, while
/// not willing to completely write {@link Object::equals} and {@link Object::hashCode} methods from
/// scratch, you can wrap those fields in {@link UV}.
///
/// {@snippet :
/// record Example(String importantField, UV<Object> unimportantField) {}
/// }
///
/// **Important note:** DO NOT use {@link UV} for map keys or set elements, and DO NOT call
/// {@link UV::equals} or {@link UV::hashCode} directly. When you want to compare the underlying
/// elements, compare them directly instead of comparing their {@code UV} wrappers.
///
/// {@snippet :
///UV<String> uv1 = new UV<>("hello");
/// UV<String> uv2 = new UV<>("world");
///
/// // WRONG: uv1.equals(uv2)  // always true
/// // WRONG: uv1 == uv2       // always false
///
/// // CORRECT:
/// uv1.e.equals(uv2.e);
/// }
@SuppressWarnings("ClassCanBeRecord")
public final class UV<T> {
    public final @NotNull T e;

    public UV(@NotNull T e) {
        this.e = e;
    }

    public static <T> @NotNull UV<T> of(@NotNull T e) {
        return new UV<>(e);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return o != null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return e.toString();
    }
}
