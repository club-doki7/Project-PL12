package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.util.ConsRevList;
import club.doki7.pl12.util.ImmSeq;
import org.junit.jupiter.api.Test;

class EvalTest {
    @Test
    void testEvalId() {
        Term id = new Term.Lam(ImmSeq.of("x"), new Term.Bound(0, "x"));
        Term k = new Term.Lam(ImmSeq.of("x", "y"), new Term.Bound(1, "x"));
        Term app = new Term.App(k, ImmSeq.of(id));

        Eval eval = Eval.make(Env.empty());
        System.out.println(eval.eval(id));
        System.out.println(eval.eval(k));
        System.out.println(eval.reify(eval.eval(app)));
    }
}
