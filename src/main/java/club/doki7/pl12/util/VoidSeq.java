package club.doki7.pl12.util;

public final class VoidSeq<T> implements Seq<T> {
    public int size;

    public VoidSeq(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
}
