package club.doki7.pl12.util;

import club.doki7.pl12.ann.PreferStaticMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@PreferStaticMethod({"snoc", "nil", "of", "from"})
public sealed interface SnocList<T> {
    @NotNull
    T revGet(int index);

    int length();

    List<T> toList();

    T[] toArray();

    boolean anyOf(Predicate<T> predicate);

    record Snoc<T>(@NotNull SnocList<T> init, @NotNull T last, int len)
        implements SnocList<T>
    {
        @Override
        public @NotNull T revGet(int index) {
            if (index == 0) {
                return last;
            }

            SnocList<T> current = init;
            int currentIndex = index - 1;
            while (current instanceof Snoc<T>(SnocList<T> init1, T last1, _)) {
                if (currentIndex == 0) {
                    return last1;
                }
                current = init1;
                currentIndex--;
            }

            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int length() {
            return len;
        }

        @Override
        public @NotNull List<T> toList() {
            List<T> elements = new ArrayList<>(len);
            SnocList<T> current = this;
            while (current instanceof Snoc<T>(SnocList<T> init1, T last1, _)) {
                elements.add(last1);
                current = init1;
            }
            return elements.reversed();
        }

        @Override
        public @NotNull T[] toArray() {
            @SuppressWarnings("unchecked") T[] array = (T[]) new Object[len];
            SnocList<T> current = this;
            while (current instanceof Snoc<T>(SnocList<T> init1, T last1, int len1)) {
                array[len1 - 1] = last1;
                current = init1;
            }
            return array;
        }

        @Override
        public boolean anyOf(Predicate<T> predicate) {
            SnocList<T> current = this;
            while (current instanceof Snoc<T>(SnocList<T> init1, T last1, _)) {
                if (predicate.test(last1)) {
                    return true;
                }
                current = init1;
            }
            return false;
        }

        @Override
        public @NotNull String toString() {
            List<String> elements = new ArrayList<>(len);
            SnocList<T> current = this;
            while (current instanceof Snoc<T>(SnocList<T> init1, T last1, _)) {
                elements.add(last1.toString());
                current = init1;
            }

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = elements.size() - 1; i >= 0; i--) {
                sb.append(elements.get(i));
                if (i != 0) {
                    sb.append(", ");
                }
            }
            sb.append(']');
            return sb.toString();
        }
    }

    final class Nil<T> implements SnocList<T> {
        private static final Nil<?> INSTANCE = new Nil<>();

        @Override
        public @NotNull T revGet(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public @NotNull List<T> toList() {
            return List.of();
        }

        @Override
        public @NotNull T[] toArray() {
            @SuppressWarnings("unchecked") T[] array = (T[]) new Object[0];
            return array;
        }

        @Override
        public boolean anyOf(Predicate<T> predicate) {
            return false;
        }

        @Override
        public @NotNull String toString() {
            return "[]";
        }
    }

    static @NotNull <T> Snoc<T> snoc(@NotNull SnocList<T> head, @NotNull T tail) {
        return new Snoc<>(head, tail, head.length() + 1);
    }

    static <T> SnocList.@NotNull Nil<T> nil() {
        @SuppressWarnings("unchecked")
        Nil<T> instance = (Nil<T>) Nil.INSTANCE;
        return instance;
    }

    @SafeVarargs
    static <T> @NotNull SnocList<T> of(@NotNull T... elements) {
        return from(elements);
    }

    static <T> @NotNull SnocList<T> from(@NotNull T[] array) {
        SnocList<T> list = nil();
        for (T element : array) {
            list = snoc(list, element);
        }
        return list;
    }

    static <T> @NotNull SnocList<T> from(@NotNull List<@NotNull T> list) {
        SnocList<T> snocList = nil();
        for (T element : list) {
            snocList = snoc(snocList, element);
        }
        return snocList;
    }
}
