package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface ConsList<T> {
    @NotNull
    T get(int index);

    int length();

    record Cons<T>(@NotNull T head, @NotNull ConsList<T> tail) implements ConsList<T> {
        @Override
        public @NotNull T get(int index) {
            if (index == 0) {
                return head;
            }

            ConsList<T> current = tail;
            int currentIndex = index - 1;
            while (current instanceof Cons<T>(T head1, ConsList<T> tail1)) {
                if (currentIndex == 0) {
                    return head1;
                }
                current = tail1;
                currentIndex--;
            }

            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int length() {
            int len = 0;
            ConsList<T> current = this;
            while (current instanceof Cons<T>(_, ConsList<T> tail1)) {
                len++;
                current = tail1;
            }
            return len;
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            ConsList<T> current = this;
            boolean first = true;
            while (current instanceof Cons<T>(T head1, ConsList<T> tail1)) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(head1);
                first = false;
                current = tail1;
            }
            sb.append(']');
            return sb.toString();
        }
    }

    final class Nil<T> implements ConsList<T> {
        private static final Nil<?> INSTANCE = new Nil<>();

        @Override
        public @NotNull T get(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public @NotNull String toString() {
            return "[]";
        }
    }

    static <T> @NotNull Cons<T> cons(@NotNull T head,
                                     @NotNull ConsList<T> tail) {
        return new Cons<>(head, tail);
    }

    static <T> @NotNull Nil<T> nil() {
        @SuppressWarnings("unchecked")
        Nil<T> instance = (Nil<T>) Nil.INSTANCE;
        return instance;
    }

    static <T> @NotNull ConsList<T> of(@NotNull T... elements) {
        return from(elements);
    }

    static <T> @NotNull ConsList<T> from(@NotNull T[] array) {
        ConsList<T> list = nil();
        for (int i = array.length - 1; i >= 0; i--) {
            list = cons(array[i], list);
        }
        return list;
    }

    static <T> @NotNull ConsList<T> from(@NotNull List<@NotNull T> list) {
        ConsList<T> consList = nil();
        for (int i = list.size() - 1; i >= 0; i--) {
            consList = cons(list.get(i), consList);
        }
        return consList;
    }
}
