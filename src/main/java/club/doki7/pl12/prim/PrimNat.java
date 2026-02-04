package club.doki7.pl12.prim;

import java.math.BigInteger;

public sealed interface PrimNat {
    record Long(long value) implements PrimNat {}
    record Big(BigInteger value) implements PrimNat {}
}
