package club.doki7.pl12.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ImmSeq<T> extends AbstractImmSeq<T> implements List<T>, RandomAccess {
    private final T @NotNull[] array;
    private final int start;
    private final int end;

    private ImmSeq(T @NotNull[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    public static <T> @NotNull ImmSeq<T> ofUnsafe(T @NotNull[] elements) {
        if (elements.length == 0) {
            //noinspection unchecked
            return (ImmSeq<T>) EMPTY;
        } else {
            return new ImmSeq<>(elements, 0, elements.length);
        }
    }

    @SafeVarargs
    public static <T> @NotNull ImmSeq<T> of(T @NotNull... elements) {
        return ofUnsafe(elements);
    }

    public static <T> @NotNull ImmSeq<T> of(List<T> list) {
        if (list instanceof ImmSeq<T> immSeq) {
            return immSeq;
        } else if (list.isEmpty()) {
            //noinspection unchecked
            return (ImmSeq<T>) EMPTY;
        } else {
            //noinspection unchecked
            T[] array = (T[]) new Object[list.size()];
            list.toArray(array);
            return ImmSeq.ofUnsafe(array);
        }
    }

    public static <T> @NotNull ImmSeq<T> nil() {
        //noinspection unchecked
        return (ImmSeq<T>) EMPTY;
    }

    public static <T> @NotNull ImmSeq<T> concat(@NotNull ImmSeq<T> a, @NotNull ImmSeq<T> b) {
        if (a.isEmpty()) {
            return b;
        } else if (b.isEmpty()) {
            return a;
        } else {
            //noinspection unchecked
            T[] newArray = (T[]) new Object[a.size() + b.size()];
            System.arraycopy(a.array, a.start, newArray, 0, a.size());
            System.arraycopy(b.array, b.start, newArray, a.size(), b.size());
            return ImmSeq.ofUnsafe(newArray);
        }
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public boolean isEmpty() {
        return start == end;
    }

    @Override
    public T get(int index) {
        if (index < 0 || start + index >= end) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return array[start + index];
        }
    }

    @Override
    public int indexOf(Object o) {
        for (int i = start; i < end; i++) {
            if (array[i].equals(o)) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = end - 1; i >= start; i--) {
            if (Objects.equals(array[i], o)) {
                return i - start;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = start; i < end; i++) {
            if (Objects.equals(array[i], o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ImmSeqIter<>(array, start, end);
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        return new ImmSeqIter<>(array, start, end);
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        if (index < 0 || start + index > end) {
            throw new IndexOutOfBoundsException(index);
        } else {
            ImmSeqIter<T> iter = new ImmSeqIter<>(array, start, end);
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            return iter;
        }
    }

    @Override
    public @NotNull ImmSeq<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex);
        } else if (fromIndex == toIndex) {
            //noinspection unchecked
            return (ImmSeq<T>) EMPTY;
        } else {
            return new ImmSeq<>(array, start + fromIndex, start + toIndex);
        }
    }

    public @NotNull ImmSeq<T> subList(int fromIndex) {
        return subList(fromIndex, size());
    }

    @Override
    public Object @NotNull[] toArray() {
        if (isEmpty()) {
            return EMPTY.array;
        }

        Object[] result = new Object[size()];
        System.arraycopy(array, start, result, 0, size());
        return result;
    }

    @Override
    public <T1> T1 @NotNull[] toArray(T1 @NotNull[] a) {
        throw new UnsupportedOperationException("ImmSeq.toArray(T[]) is not implemented");
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = start; i < end; i++) {
            sb.append(array[i]);
            if (i != end - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

    static final ImmSeq<Object> EMPTY = new ImmSeq<>(new Object[0], 0, 0);
    static final String IMM_SEQ_IS_IMMUTABLE = "ImmSeq is immutable";
}

final class ImmSeqIter<T> implements ListIterator<T> {
    private final @NotNull T[] array;
    private final int start;
    private final int end;
    private int current;


    ImmSeqIter(@NotNull T[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;

        this.current = start;
    }

    @Override
    public boolean hasNext() {
        return current < end;
    }

    @Override
    public T next() {
        T result = array[current];
        current++;
        return result;
    }

    @Override
    public boolean hasPrevious() {
        return current > start;
    }

    @Override
    public T previous() {
        current--;
        return array[current];
    }

    @Override
    public int nextIndex() {
        return current - start;
    }

    @Override
    public int previousIndex() {
        return current - start - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public void set(T t) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public void add(T t) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }
}

abstract sealed class AbstractImmSeq<T> implements List<T> {
    @Override
    public final boolean add(T t) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final void add(int index, T element) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final boolean remove(Object o) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException(ImmSeq.IMM_SEQ_IS_IMMUTABLE);
    }
}
