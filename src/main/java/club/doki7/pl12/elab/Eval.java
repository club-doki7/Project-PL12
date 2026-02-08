package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;

public final class Eval {
    public static Value eval(Env env, ConsRevList<ImmSeq<Value>> localEnv, Term term) {
        while (true) {
            switch (term) {
                case Term.Ann(Term annotated, _) -> term = annotated;
                case Term.SolvedMeta(_, Term solution) -> term = solution;

                case Term.Bound(int index, _) -> {
                    return DBI.get(localEnv, index);
                }
                case Term.Free free -> {
                    if (free.name() instanceof Name.Global(String name)) {
                        Env.Entry entry = env.lookup(name);
                        if (entry == null) {
                            throw new IllegalStateException("Unbound global: " + name);
                        }
                        return entry.value();
                    }

                    return new Value.Rigid(free, ImmSeq.nil());
                }
                case Term.Meta meta -> {
                    return new Value.Flex(meta, ImmSeq.nil());
                }
                case Term.Lam(ImmSeq<String> names, Term body) -> {
                    return new Value.Lam(localEnv, names, body);
                }
                case Term.Pi(ImmSeq<Term.ParamGroupTm> paramGroups, Term body) -> {
                    Term.ParamGroupTm firstGroup = paramGroups.getFirst();
                    ImmSeq<String> names = firstGroup.names();
                    Type paramType = Type.ofVal(eval(env, localEnv, firstGroup.type()));
                    Term newBody = paramGroups.size() == 1
                        ? body
                        : new Term.Pi(paramGroups.subList(1), body);
                    return new Value.Pi(localEnv, names, paramType, newBody);
                }
                case Term.Univ _ -> {
                    return Value.Univ.UNIV;
                }
                case Term.App(Term func, ImmSeq<Term> args) -> {
                    Value funcValue = eval(env, localEnv, func);
                    Value[] argValues = new Value[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        argValues[i] = eval(env, localEnv, args.get(i));
                    }

                    return apply(env, funcValue, ImmSeq.ofUnsafe(argValues));
                }
            }
        }
    }

    private static Value apply(Env env, Value funcValue, ImmSeq<Value> args) {
        while (!args.isEmpty()) {
            switch (funcValue) {
                case Value.Flex(Term.Meta head, ImmSeq<Value> args0) -> {
                    return new Value.Flex(head, ImmSeq.concat(args0, args));
                }
                case Value.Rigid(Value.RigidHead head, ImmSeq<Value> args0) -> {
                    return new Value.Rigid(head, ImmSeq.concat(args0, args));
                }
                case Value.Lam(ConsRevList<ImmSeq<Value>> localEnv,
                               ImmSeq<String> paramNames,
                               Term body) -> {
                    if (paramNames.size() > args.size()) {
                        throw new IllegalStateException("Not enough arguments to apply Lam");
                    }
                    ImmSeq<Value> appliedArgs = args.subList(0, paramNames.size());
                    funcValue = eval(env, ConsRevList.rcons(localEnv, appliedArgs), body);
                    args = args.subList(paramNames.size());
                }
                case Value.Pi _ -> throw new IllegalStateException("Cannot apply a Pi type");
                case Value.Univ _ -> throw new IllegalStateException("Cannot apply a Univ type");
            }
        }
        return funcValue;
    }
}
