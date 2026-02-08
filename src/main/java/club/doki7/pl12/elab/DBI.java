package club.doki7.pl12.elab;

import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;

public final class DBI {
    public static <T> T get(ConsRevList<ImmSeq<T>> ctx, int index) {
        while (true) {
            if (!(ctx instanceof ConsRevList.Cons<ImmSeq<T>>(ConsRevList<ImmSeq<T>> init,
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
