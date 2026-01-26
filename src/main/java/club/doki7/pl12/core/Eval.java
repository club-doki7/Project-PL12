package club.doki7.pl12.core;

import club.doki7.pl12.util.ConsRevList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Eval {
    public static Value eval(Term term, Env env, ConsRevList<Value> localEnv) {
        return switch (term) {
            case Term.LamChk(Term.Checkable body, _, _) ->
                    new Value.Lam(env, localEnv, body, term);
            case Term.LamInf(_, Term.Checkable body, _, _) ->
                    new Value.Lam(env, localEnv, body, term);
            case Term.Ann(Term.Checkable annotated, _, _) -> eval(annotated, env, localEnv);
            case Term.Univ univ -> new Value.Univ(univ);
            case Term.Pi(_, Term.Checkable paramType, Term.Checkable bodyType, _, _) ->
                    new Value.Pi(env,
                                 Type.ofVal(eval(paramType, env, localEnv)),
                                 localEnv,
                                 bodyType,
                                 (Term.Pi) term);
            case Term.Bound(Name.Local name, _) -> localEnv.revGet(name.index());
            case Term.Free(Name name, _) -> {
                if (name instanceof Name.Global(String strName)) {
                    @Nullable Env.Entry entry = env.lookup(strName);
                    if (entry != null) {
                        yield entry.value();
                    } else {
                        throw new IllegalStateException("Unbound global name should have been"
                                                        + "rejected by the type checker.");
                    }
                } else {
                    yield new Value.Rigid(name, term);
                }
            }
            case Term.App(Term.Inferable head, Term.Checkable arg, _) -> {
                Value vHead = eval(head, env, localEnv);
                Value vArg = eval(arg, env, localEnv);
                yield app(vHead, vArg, term);
            }
            case Term.Meta meta -> new Value.Flex(meta);
        };
    }

    public static Value app(Value head, Value arg, Term appTerm) {
        return switch (head) {
            case Value.Flex(Term.Meta meta, ConsRevList<Value> headSpine, _) -> {
                ConsRevList<Value> newSpine = new ConsRevList.Cons<>(headSpine, arg);
                yield new Value.Flex(meta, newSpine, appTerm);
            }
            case Value.Rigid(Name name, ConsRevList<Value> headSpine, _) -> {
                ConsRevList<Value> newSpine = new ConsRevList.Cons<>(headSpine, arg);
                yield new Value.Rigid(name, newSpine, appTerm);
            }
            case Value.Lam(Env env, ConsRevList<Value> localEnv, Term body, _) -> {
                ConsRevList<Value> newLocalEnv = new ConsRevList.Cons<>(localEnv, arg);
                yield eval(body, env, newLocalEnv);
            }
            default -> throw new IllegalStateException("Cannot apply to head: " + head);
        };
    }

    public static Value app(Value head, ConsRevList<Value> spine, Term appTerm) {
        List<Value> spineList = spine.toList();

        Value result = head;
        for (Value value : spineList) {
            result = app(result, value, appTerm);
        }
        return result;
    }
}
