package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

public interface Value {
    interface Closure {
        @NotNull ConsRevList<ImmSeq<Value>> localEnv();
        @NotNull Term body();
    }
}
