package club.doki7.pl12.elab;

import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;

public final class DBI {
    public static <T> T get(SnocList<ImmSeq<T>> ctx, int index) {
        while (true) {
            if (!(ctx instanceof SnocList.Snoc(SnocList<ImmSeq<T>> init,
                                               ImmSeq<T> last,
                                               _))) {
                throw new IndexOutOfBoundsException(index);
            }

            if (index < last.size()) {
                return last.get(last.size() - 1 - index);
            } else {
                index -= last.size();
                ctx = init;
            }
        }
    }
}
