package club.doki7.pl12.elab;

import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DBI {
    public static int findInContext(@NotNull String name, @NotNull List<String> ctx) {
        for (int i = ctx.size() - 1; i >= 0; i--) {
            if (ctx.get(i).equals(name)) {
                return ctx.size() - 1 - i;
            }
        }
        return -1;
    }

    public static <T> T get(int index, ConsRevList<ImmSeq<T>> ctx) {
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
