package club.doki7.pl12.elab;

import club.doki7.pl12.util.Pair;
import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class DBI {
    public static <T> T get(SnocList<ImmSeq<T>> ctx, int index) {
        while (ctx instanceof SnocList.Snoc(SnocList<ImmSeq<T>> init, ImmSeq<T> last, _)) {
            if (index < last.size()) {
                return last.get(last.size() - 1 - index);
            } else {
                index -= last.size();
                ctx = init;
            }
        }

        throw new IndexOutOfBoundsException(index);
    }

    public static <T> @Nullable Pair<Integer, T> find(SnocList<ImmSeq<T>> ctx, Predicate<T> pred) {
        int acc = 0;
        while (ctx instanceof SnocList.Snoc(SnocList<ImmSeq<T>> init, ImmSeq<T> last, _)) {
            for (int i = last.size() - 1; i >= 0; i--) {
                T item = last.get(i);
                if (pred.test(item)) {
                    return new Pair<>(acc + (last.size() - 1 - i), item);
                }
            }
            acc += last.size();
            ctx = init;
        }

        return null;
    }

    public static <T> @Nullable Integer find(SnocList<ImmSeq<T>> ctx, T item) {
        int acc = 0;
        while (ctx instanceof SnocList.Snoc(SnocList<ImmSeq<T>> init, ImmSeq<T> last, _)) {
            for (int i = last.size() - 1; i >= 0; i--) {
                if (last.get(i).equals(item)) {
                    return acc + (last.size() - 1 - i);
                }
            }
            acc += last.size();
            ctx = init;
        }
        return null;
    }
}
