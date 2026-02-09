package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ImmSeq;
import org.junit.jupiter.api.Test;

class EvalTest {
    @Test
    void testEvalSelect2() {
        Env env = Env.empty();
        Eval eval = Eval.make(env);

        Term select2 = new Term.Lam(ImmSeq.of("a", "b", "c", "d"), new Term.Bound(2, "b"));
        System.out.println("select2 = " + select2);
        Value select2Value = eval.eval(select2);
        System.out.println("evaluated = " + select2Value);
        Term select2Reified = eval.reify(select2Value);
        System.out.println("reified = " + select2Reified);
    }
}
