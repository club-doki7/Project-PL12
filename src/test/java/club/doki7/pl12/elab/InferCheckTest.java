package club.doki7.pl12.elab;

import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.exc.TypeCheckException;
import club.doki7.pl12.syntax.Expr;
import club.doki7.pl12.syntax.ParseContext;
import club.doki7.pl12.syntax.Parser;
import org.junit.jupiter.api.Test;

class InferCheckTest {
    @Test
    void inferCheckTestSimple() throws ParseException, TypeCheckException {
        ParseContext ctx = ParseContext.of("forall (x y : type), x -> y -> x", "<test>");
        Expr expr = Parser.parseExpr(ctx).first();
        Env env = Env.empty();
        Context inferCtx = Context.make(env);

        System.out.println(InferCheck.infer(inferCtx, expr));
    }
}
