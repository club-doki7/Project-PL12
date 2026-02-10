package club.doki7.pl12.util;

public sealed interface Seq<T> permits ImmSeq, VoidSeq {
    int size();
}
