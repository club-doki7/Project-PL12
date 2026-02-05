package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public sealed interface ConsRevList<T> {
    @NotNull
    T revGet(int index);

    int length();

    List<T> toList();

    boolean anyOf(Predicate<T> predicate);

    record Cons<T>(@NotNull ConsRevList<T> init, @NotNull T last, int len) implements ConsRevList<T> {
        @Override
        public @NotNull T revGet(int index) {
            if (index == 0) {
                return last;
            }

            ConsRevList<T> current = init;
            int currentIndex = index - 1;
            while (current instanceof Cons<T>(ConsRevList<T> init1, T last1, _)) {
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
            ConsRevList<T> current = this;
            while (current instanceof Cons<T>(ConsRevList<T> init1, T last1, _)) {
                elements.add(last1);
                current = init1;
            }
            return elements.reversed();
        }

        @Override
        public boolean anyOf(Predicate<T> predicate) {
            ConsRevList<T> current = this;
            while (current instanceof Cons<T>(ConsRevList<T> init1, T last1, _)) {
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
            ConsRevList<T> current = this;
            while (current instanceof Cons<T>(ConsRevList<T> init1, T last1, _)) {
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

    final class Nil<T> implements ConsRevList<T> {
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
        public boolean anyOf(Predicate<T> predicate) {
            return false;
        }

        @Override
        public @NotNull String toString() {
            return "[]";
        }
    }

    static <T> ConsRevList.@NotNull Cons<T> rcons(@NotNull ConsRevList<T> head, @NotNull T tail) {
        return new Cons<>(head, tail, head.length() + 1);
    }

    static <T> ConsRevList.@NotNull Nil<T> nil() {
        @SuppressWarnings("unchecked")
        Nil<T> instance = (Nil<T>) Nil.INSTANCE;
        return instance;
    }

    @SafeVarargs
    static <T> @NotNull ConsRevList<T> of(@NotNull T... elements) {
        return from(elements);
    }

    static <T> @NotNull ConsRevList<T> from(@NotNull T[] array) {
        ConsRevList<T> list = nil();
        for (T element : array) {
            list = rcons(list, element);
        }
        return list;
    }

    static <T> @NotNull ConsRevList<T> from(@NotNull List<@NotNull T> list) {
        ConsRevList<T> consRevList = nil();
        for (T element : list) {
            consRevList = rcons(consRevList, element);
        }
        return consRevList;
    }
}
