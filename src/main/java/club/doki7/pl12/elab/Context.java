package club.doki7.pl12.elab;

import club.doki7.pl12.core.Type;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;

public record Context(int level,
                      Env env,
                      SnocList<ImmSeq<Value>> localEnv,
                      SnocList<ImmSeq<Type>> types)
{
}
