package club.doki7.pl12.elab;

import club.doki7.pl12.core.Type;
import club.doki7.pl12.util.SnocList;
import club.doki7.pl12.util.ImmSeq;

public record Context(Env env,
                      SnocList<ImmSeq<String>> dbiEnv,
                      SnocList<ImmSeq<Type>> types,
                      MetaContext mctx)
{
    public Context bind(ImmSeq<String> names, ImmSeq<Type> types) {
        assert names.size() == types.size();
        return new Context(env,
                           SnocList.snoc(dbiEnv, names),
                           SnocList.snoc(this.types, types),
                           mctx);
    }
}
