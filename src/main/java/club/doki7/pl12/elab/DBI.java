package club.doki7.pl12.elab;

import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;

public final class DBI {
    public static Value get(ConsRevList<ImmSeq<Value>> ctx, int index) {
        while (true) {
            if (!(ctx instanceof ConsRevList.Cons<ImmSeq<Value>>(ConsRevList<ImmSeq<Value>> init,
                                                                 ImmSeq<Value> last,
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
