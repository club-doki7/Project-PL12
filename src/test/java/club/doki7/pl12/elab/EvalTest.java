package club.doki7.pl12.elab;

import club.doki7.pl12.core.Term;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.util.ImmSeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvalTest {
    static Term churchNat(int n) {
        Term result = new Term.Bound(0, "x");
        for (int i = 0; i < n; i++) {
            result = new Term.App(new Term.Bound(1, "f"), result);
        }
        return new Term.Lam(ImmSeq.of("f", "x"), result);
    }

    static Term ZERO = churchNat(0);
    static Term SUCC = new Term.Lam(ImmSeq.of("n", "f", "x"),
                                    new Term.App(new Term.Bound(1, "f"),
                                                 new Term.App(new Term.Bound(2, "n"),
                                                              new Term.Bound(1, "f"),
                                                              new Term.Bound(0, "x"))));

    static Term ADD = new Term.Lam(ImmSeq.of("m", "n", "f", "x"),
                                   new Term.App(new Term.Bound(3, "m"),
                                                new Term.Bound(1, "f"),
                                                new Term.App(new Term.Bound(2, "n"),
                                                             new Term.Bound(1, "f"),
                                                             new Term.Bound(0, "x"))));

    static Term MUL = new Term.Lam(ImmSeq.of("m", "n", "f"),
                                   new Term.App(new Term.Bound(2, "m"),
                                                new Term.App(new Term.Bound(1, "n"),
                                                             new Term.Bound(0, "f"))));

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

    @Test
    void testSuccZeroIsOne() {
        Env env = Env.empty();
        Eval eval = Eval.make(env);

        Term succZero = new Term.App(SUCC, ZERO);
        Assertions.assertEquals(churchNat(1), eval.reify(eval.eval(succZero)));
    }

    @Test
    void testChurchAddition() {
        Env env = Env.empty();
        Eval eval = Eval.make(env);

        Term two = churchNat(2);
        Term three = churchNat(3);
        Term addTwoThree = new Term.App(ADD, two, three);
        Assertions.assertEquals(churchNat(5), eval.reify(eval.eval(addTwoThree)));

        Term addZeroZero = new Term.App(ADD, ZERO, ZERO);
        Assertions.assertEquals(churchNat(0), eval.reify(eval.eval(addZeroZero)));

        Term one = churchNat(1);
        Term addOneZero = new Term.App(ADD, one, ZERO);
        Assertions.assertEquals(churchNat(1), eval.reify(eval.eval(addOneZero)));
    }

    @Test
    void testChurchMultiplication() {
        Env env = Env.empty();
        Eval eval = Eval.make(env);

        Term two = churchNat(2);
        Term three = churchNat(3);
        Term mulTwoThree = new Term.App(MUL, two, three);
        Assertions.assertEquals(churchNat(6), eval.reify(eval.eval(mulTwoThree)));

        Term mulZeroTwo = new Term.App(MUL, ZERO, two);
        Assertions.assertEquals(churchNat(0), eval.reify(eval.eval(mulZeroTwo)));

        Term mulOneThree = new Term.App(MUL, churchNat(1), three);
        Assertions.assertEquals(churchNat(3), eval.reify(eval.eval(mulOneThree)));
    }
}
