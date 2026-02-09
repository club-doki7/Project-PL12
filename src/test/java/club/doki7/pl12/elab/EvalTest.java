package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ImmSeq;
import org.junit.jupiter.api.Test;

class EvalTest {
    @Test
    void testEvalId() {
        Term id = new Term.Lam(ImmSeq.of("x"), new Term.Bound(0, "x"));
        Term k2 = new Term.Lam(ImmSeq.of("x", "y", "z"), new Term.Bound(2, "x"));
        Term app = new Term.App(k2, ImmSeq.of(id));

        Eval eval = Eval.make(Env.empty());

        Value vapp = eval.eval(app);
        System.out.println("vapp = " + vapp);
        Term reap = eval.reify(vapp);
        System.out.println("reap = " + reap);
    }
}
