package club.doki7.pl12.elab;

import club.doki7.pl12.core.Name;
import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Type;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;

public final class Eval {
    public static Eval make(Env env) {
        return new Eval(env);
    }

    public Value eval(Term term) {
        return eval(ConsRevList.nil(), term);
    }

    public Term reify(Value value) {
        return reify(0, value);
    }

    public Term reify(Type type) {
        return reify(0, type.value());
    }

    private Value eval(ConsRevList<ImmSeq<Value>> localEnv, Term term) {
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
                    return new Value.Rigid(new Value.Lam(localEnv, names, body), ImmSeq.nil());
                }
                case Term.Pi(ImmSeq<String> names, Term type, Term body) -> {
                    Type typeVal = Type.ofVal(eval(localEnv, type));
                    return new Value.Pi(localEnv, names, typeVal, body);
                }
                case Term.Univ _ -> {
                    return Value.Univ.UNIV;
                }
                case Term.App(Term func, ImmSeq<Term> args) -> {
                    Value funcValue = eval(localEnv, func);
                    Value[] argValues = new Value[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        argValues[i] = eval(localEnv, args.get(i));
                    }

                    return apply(funcValue, ImmSeq.ofUnsafe(argValues));
                }
            }
        }
    }

    private Value apply(Value funcValue, ImmSeq<Value> args) {
        while (!args.isEmpty()) {
            switch (funcValue) {
                case Value.Flex(Term.Meta head, ImmSeq<Value> args0) -> {
                    return new Value.Flex(head, ImmSeq.concat(args0, args));
                }
                case Value.Rigid(Value.RigidHead head, ImmSeq<Value> args0) -> {
                    ImmSeq<Value> allArgs = ImmSeq.concat(args0, args);
                    if (!(head instanceof Value.Lam lam)) {
                        return new Value.Rigid(head, allArgs);
                    }

                    if (lam.paramNames().size() > allArgs.size()) {
                        return new Value.Rigid(lam, allArgs);
                    }

                    ImmSeq<Value> appliedArgs = allArgs.subList(0, lam.paramNames().size());
                    funcValue = eval(ConsRevList.rcons(lam.localEnv(), appliedArgs), lam.body());
                    args = allArgs.subList(lam.paramNames().size());
                }
                case Value.Pi _ -> throw new IllegalStateException("Cannot apply a Pi type");
                case Value.Univ _ -> throw new IllegalStateException("Cannot apply a Univ type");
            }
        }
        return funcValue;
    }

    private Term reify(int level, Value value) {
        return switch (value) {
            case Value.Flex(Term.Meta head, ImmSeq<Value> spine) -> reifySpine(level, head, spine);
            case Value.Rigid(Value.RigidHead head, ImmSeq<Value> spine) -> switch (head) {
                case Value.Lam lam -> {
                    if (!spine.isEmpty()) {
                        lam = forcePartial(lam, spine);
                    }
                    yield reifyLam(level, lam);
                }
                case Term.Bound bound -> reifySpine(level, bound, spine);
                case Term.Free free -> reifySpine(level, free, spine);
            };
            case Value.Pi pi -> reifyPi(level, pi);
            case Value.Univ _ -> Term.Univ.UNIV;
        };
    }

    private Term reify(int level, Type type) {
        return reify(level, type.value());
    }

    private Term reifySpine(int level, Term head, ImmSeq<Value> spine) {
        if (head instanceof Term.Free(Name.Quote(int quotedLevel, String name))) {
            head = new Term.Bound(level - quotedLevel - 1, name);
        }

        if (spine.isEmpty()) {
            return head;
        }

        Term[] spineTerms = new Term[spine.size()];
        for (int i = 0; i < spine.size(); i++) {
            spineTerms[i] = reify(level, spine.get(i));
        }
        return new Term.App(head, ImmSeq.ofUnsafe(spineTerms));
    }

    private Term.Lam reifyLam(int level, Value.Lam lam) {
        Term body = reifyClosure(level, lam);
        return new Term.Lam(lam.paramNames(), body);
    }

    private Term.Pi reifyPi(int level, Value.Pi pi) {
        Term body = reifyClosure(level, pi);
        Term paramTypeTerm = reify(level, pi.paramType());
        return new Term.Pi(pi.paramNames(), paramTypeTerm, body);
    }

    private Term reifyClosure(int level, Value.Closure closure) {
        ImmSeq<String> paramNames = closure.paramNames();
        int paramCount = paramNames.size();

        Value[] freshVars = new Value[paramCount];
        for (int i = 0; i < paramCount; i++) {
            int paramLevel = level + i;
            Term.Free free = new Term.Free(new Name.Quote(paramLevel, paramNames.get(i)));
            freshVars[i] = new Value.Rigid(free, ImmSeq.nil());
        }

        ConsRevList<ImmSeq<Value>> extendedEnv = ConsRevList.rcons(closure.localEnv(),
                                                                   ImmSeq.ofUnsafe(freshVars));
        Value bodyValue = eval(extendedEnv, closure.body());
        return reify(level + paramCount, bodyValue);
    }

    private Value.Lam forcePartial(Value.Lam lam, ImmSeq<Value> args) {
        assert !args.isEmpty() && lam.paramNames().size() > args.size();
        return new Value.Lam(ConsRevList.rcons(lam.localEnv(), args),
                             lam.paramNames().subList(args.size()),
                             lam.body());
    }

    private Eval(Env env) {
        this.env = env;
    }

    private final Env env;
}
