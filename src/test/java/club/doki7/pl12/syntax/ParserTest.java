package club.doki7.pl12.syntax;

import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.util.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private Command parseCommand(String input) throws Exception {
        ParseContext ctx = ParseContext.of(input, "<test>");
        Pair<Command, ParseContext> result = Parser.parseCommand(ctx);
        return result.first();
    }

    private Expr parseExpr(String input) throws Exception {
        ParseContext ctx = ParseContext.of(input, "<test>");
        Pair<Expr, ParseContext> result = Parser.parseExpr(ctx);
        return result.first();
    }

    @Nested
    class ExpressionTests {
        @Test
        void testUniv() throws Exception {
            Expr expr = parseExpr("*");
            assertInstanceOf(Expr.Univ.class, expr);
        }

        @Test
        void testVar() throws Exception {
            Expr expr = parseExpr("x");
            assertInstanceOf(Expr.Var.class, expr);
            assertEquals("x", ((Expr.Var) expr).name().lexeme());
        }

        @Test
        void testNatLiteral() throws Exception {
            Expr expr = parseExpr("42");
            assertInstanceOf(Expr.Lit.class, expr);
        }

        @Test
        void testStringLiteral() throws Exception {
            Expr expr = parseExpr("\"hello\"");
            assertInstanceOf(Expr.Lit.class, expr);
        }

        @Test
        void testHole() throws Exception {
            Expr expr = parseExpr("??");
            assertInstanceOf(Expr.Hole.class, expr);
        }

        @Test
        void testParen() throws Exception {
            Expr expr = parseExpr("(x)");
            assertInstanceOf(Expr.Paren.class, expr);
            Expr inner = ((Expr.Paren) expr).expr();
            assertInstanceOf(Expr.Var.class, inner);
        }

        @Test
        void testNestedParen() throws Exception {
            Expr expr = parseExpr("((x))");
            assertInstanceOf(Expr.Paren.class, expr);
            Expr inner = ((Expr.Paren) expr).expr();
            assertInstanceOf(Expr.Paren.class, inner);
        }
    }

    @Nested
    class ArrowTests {
        @Test
        void testSimpleArrow() throws Exception {
            Expr expr = parseExpr("A -> B");
            assertInstanceOf(Expr.Arrow.class, expr);
            Expr.Arrow arrow = (Expr.Arrow) expr;
            assertInstanceOf(Expr.Var.class, arrow.from());
            assertInstanceOf(Expr.Var.class, arrow.to());
        }

        @Test
        void testChainedArrow() throws Exception {
            Expr expr = parseExpr("A -> B -> C");
            assertInstanceOf(Expr.Arrow.class, expr);
            Expr.Arrow arrow = (Expr.Arrow) expr;
            assertInstanceOf(Expr.Var.class, arrow.from());
            assertInstanceOf(Expr.Arrow.class, arrow.to());
        }

        @Test
        void testArrowFromUniv() throws Exception {
            Expr expr = parseExpr("* -> *");
            assertInstanceOf(Expr.Arrow.class, expr);
            Expr.Arrow arrow = (Expr.Arrow) expr;
            assertInstanceOf(Expr.Univ.class, arrow.from());
            assertInstanceOf(Expr.Univ.class, arrow.to());
        }
    }

    @Nested
    class AnnotationTests {
        @Test
        void testSimpleAnn() throws Exception {
            Expr expr = parseExpr("x : A");
            assertInstanceOf(Expr.Ann.class, expr);
            Expr.Ann ann = (Expr.Ann) expr;
            assertInstanceOf(Expr.Var.class, ann.term());
            assertInstanceOf(Expr.Var.class, ann.ann());
        }

        @Test
        void testAnnWithArrow() throws Exception {
            Expr expr = parseExpr("f : A -> B");
            assertInstanceOf(Expr.Ann.class, expr);
            Expr.Ann ann = (Expr.Ann) expr;
            assertInstanceOf(Expr.Var.class, ann.term());
            assertInstanceOf(Expr.Arrow.class, ann.ann());
        }
    }

    @Nested
    class ApplicationTests {
        @Test
        void testSimpleApp() throws Exception {
            Expr expr = parseExpr("f x");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals("f", ((Expr.Var) app.func()).name().lexeme());
            assertEquals(1, app.args().size());
        }

        @Test
        void testMultiArgApp() throws Exception {
            Expr expr = parseExpr("f x y z");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals(3, app.args().size());
        }

        @Test
        void testImplicitArg() throws Exception {
            Expr expr = parseExpr("f {A}");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals(1, app.args().size());
            assertInstanceOf(Argument.Implicit.class, app.args().get(0));
        }

        @Test
        void testNamedImplicitArg() throws Exception {
            Expr expr = parseExpr("f {x = A}");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals(1, app.args().size());
            assertInstanceOf(Argument.NamedImplicit.class, app.args().get(0));
            Argument.NamedImplicit ni = (Argument.NamedImplicit) app.args().get(0);
            assertEquals("x", ni.name().lexeme());
        }

        @Test
        void testMixedArgs() throws Exception {
            Expr expr = parseExpr("f {A} x {y = B}");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals(3, app.args().size());
            assertInstanceOf(Argument.Implicit.class, app.args().get(0));
            assertInstanceOf(Argument.Explicit.class, app.args().get(1));
            assertInstanceOf(Argument.NamedImplicit.class, app.args().get(2));
        }

        @Test
        void testAppWithParen() throws Exception {
            Expr expr = parseExpr("f (g x)");
            assertInstanceOf(Expr.App.class, expr);
            Expr.App app = (Expr.App) expr;
            assertEquals(1, app.args().size());
            Argument.Explicit arg = (Argument.Explicit) app.args().get(0);
            assertInstanceOf(Expr.Paren.class, arg.expr());
        }
    }

    @Nested
    class FunTests {
        @Test
        void testSimpleFun() throws Exception {
            Expr expr = parseExpr("fun x => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertEquals(1, fun.paramGroups().size());
            assertInstanceOf(Expr.Var.class, fun.body());
        }

        @Test
        void testFunMultipleParams() throws Exception {
            Expr expr = parseExpr("fun x y z => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertEquals(1, fun.paramGroups().size());
            assertEquals(3, fun.paramGroups().get(0).names().size());
        }

        @Test
        void testFunWithExplicitParamGroup() throws Exception {
            Expr expr = parseExpr("fun (x : A) => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertEquals(1, fun.paramGroups().size());
            assertFalse(fun.paramGroups().get(0).isImplicit());
        }

        @Test
        void testFunWithImplicitParamGroup() throws Exception {
            Expr expr = parseExpr("fun {x : A} => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertEquals(1, fun.paramGroups().size());
            assertTrue(fun.paramGroups().get(0).isImplicit());
        }

        @Test
        void testFunWithMultipleParamGroups() throws Exception {
            Expr expr = parseExpr("fun (x : A) {y : B} (z : C) => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertEquals(3, fun.paramGroups().size());
            assertFalse(fun.paramGroups().get(0).isImplicit());
            assertTrue(fun.paramGroups().get(1).isImplicit());
            assertFalse(fun.paramGroups().get(2).isImplicit());
        }

        @Test
        void testLambdaSyntax() throws Exception {
            Expr expr = parseExpr("λ x => x");
            assertInstanceOf(Expr.Fun.class, expr);
        }
    }

    @Nested
    class PiTests {
        @Test
        void testSimplePi() throws Exception {
            Expr expr = parseExpr("∀ x, x");
            assertInstanceOf(Expr.Pi.class, expr);
            Expr.Pi pi = (Expr.Pi) expr;
            assertInstanceOf(Expr.Var.class, pi.body());
        }

        @Test
        void testForallSyntax() throws Exception {
            Expr expr = parseExpr("forall x, x");
            assertInstanceOf(Expr.Pi.class, expr);
        }

        @Test
        void testPiWithExplicitParamGroup() throws Exception {
            Expr expr = parseExpr("∀ (x : A), B");
            assertInstanceOf(Expr.Pi.class, expr);
            Expr.Pi pi = (Expr.Pi) expr;
            assertFalse(pi.paramGroup().isImplicit());
        }

        @Test
        void testPiWithImplicitParamGroup() throws Exception {
            Expr expr = parseExpr("∀ {x : A}, B");
            assertInstanceOf(Expr.Pi.class, expr);
            Expr.Pi pi = (Expr.Pi) expr;
            assertTrue(pi.paramGroup().isImplicit());
        }
    }

    @Nested
    class AxiomTests {
        @Test
        void testSimpleAxiom() throws Exception {
            Command cmd = parseCommand("Axiom x : A.");
            assertInstanceOf(Command.Axiom.class, cmd);
            Command.Axiom axiom = (Command.Axiom) cmd;
            assertEquals(1, axiom.names().size());
            assertEquals("x", axiom.names().get(0).lexeme());
        }

        @Test
        void testMultipleNamesAxiom() throws Exception {
            Command cmd = parseCommand("Axiom x y z : A.");
            assertInstanceOf(Command.Axiom.class, cmd);
            Command.Axiom axiom = (Command.Axiom) cmd;
            assertEquals(3, axiom.names().size());
        }

        @Test
        void testAxiomWithArrowType() throws Exception {
            Command cmd = parseCommand("Axiom f : A -> B.");
            assertInstanceOf(Command.Axiom.class, cmd);
            Command.Axiom axiom = (Command.Axiom) cmd;
            assertInstanceOf(Expr.Arrow.class, axiom.type());
        }
    }

    @Nested
    class CheckTests {
        @Test
        void testSimpleCheck() throws Exception {
            Command cmd = parseCommand("Check x.");
            assertInstanceOf(Command.Check.class, cmd);
            Command.Check check = (Command.Check) cmd;
            assertInstanceOf(Expr.Var.class, check.expr());
        }

        @Test
        void testCheckApp() throws Exception {
            Command cmd = parseCommand("Check f x.");
            assertInstanceOf(Command.Check.class, cmd);
            Command.Check check = (Command.Check) cmd;
            assertInstanceOf(Expr.App.class, check.expr());
        }

        @Test
        void testCheckAnnotated() throws Exception {
            Command cmd = parseCommand("Check x : A.");
            assertInstanceOf(Command.Check.class, cmd);
            Command.Check check = (Command.Check) cmd;
            assertInstanceOf(Expr.Ann.class, check.expr());
        }
    }

    @Nested
    class DefinitionTests {
        @Test
        void testSimpleDefinition() throws Exception {
            Command cmd = parseCommand("Definition id : A -> A := fun x => x.");
            assertInstanceOf(Command.Definition.class, cmd);
            Command.Definition def = (Command.Definition) cmd;
            assertEquals("id", def.name().lexeme());
            assertEquals(0, def.paramGroups().size());
            assertFalse(def.isProcedure());
        }

        @Test
        void testDefinitionWithParams() throws Exception {
            Command cmd = parseCommand("Definition id (x : A) : A := x.");
            assertInstanceOf(Command.Definition.class, cmd);
            Command.Definition def = (Command.Definition) cmd;
            assertEquals(1, def.paramGroups().size());
        }

        @Test
        void testDefinitionWithMultipleParamGroups() throws Exception {
            Command cmd = parseCommand("Definition f (x : A) {y : B} (z : C) : D := x.");
            assertInstanceOf(Command.Definition.class, cmd);
            Command.Definition def = (Command.Definition) cmd;
            assertEquals(3, def.paramGroups().size());
        }

        @Test
        void testProcedure() throws Exception {
            Command cmd = parseCommand("Procedure foo : A := x.");
            assertInstanceOf(Command.Definition.class, cmd);
            Command.Definition def = (Command.Definition) cmd;
            assertTrue(def.isProcedure());
        }
    }

    @Nested
    class NotationTests {
        @Test
        void testLeftAssocNotation() throws Exception {
            Command cmd = parseCommand("Notation left 50 (+) := add.");
            assertInstanceOf(Command.Notation.class, cmd);
            Command.Notation notation = (Command.Notation) cmd;
            assertEquals("+", notation.name().lexeme());
            assertEquals(Operator.Assoc.LEFT, notation.assoc());
            assertEquals(50, notation.prec());
        }

        @Test
        void testRightAssocNotation() throws Exception {
            Command cmd = parseCommand("Notation right 60 (^) := pow.");
            assertInstanceOf(Command.Notation.class, cmd);
            Command.Notation notation = (Command.Notation) cmd;
            assertEquals(Operator.Assoc.RIGHT, notation.assoc());
            assertEquals(60, notation.prec());
        }

        @Test
        void testNoassocNotation() throws Exception {
            Command cmd = parseCommand("Notation noassoc 40 (==) := eq.");
            assertInstanceOf(Command.Notation.class, cmd);
            Command.Notation notation = (Command.Notation) cmd;
            assertEquals(Operator.Assoc.NONE, notation.assoc());
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void testUnexpectedToken() {
            assertThrows(ParseException.class, () -> parseCommand(":= x"));
        }

        @Test
        void testMissingDot() {
            assertThrows(ParseException.class, () -> parseCommand("Axiom x : A"));
        }

        @Test
        void testMissingColon() {
            assertThrows(ParseException.class, () -> parseCommand("Axiom x A."));
        }

        @Test
        void testUnmatchedParen() {
            assertThrows(ParseException.class, () -> parseExpr("(x"));
        }

        @Test
        void testEmptyParen() {
            assertThrows(ParseException.class, () -> parseExpr("()"));
        }

        @Test
        void testInvalidAssoc() {
            assertThrows(ParseException.class, () -> parseCommand("Notation invalid 50 (+) := add."));
        }
    }

    @Nested
    class ComplexExpressionTests {
        @Test
        void testNestedFun() throws Exception {
            Expr expr = parseExpr("fun x => fun y => x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun outer = (Expr.Fun) expr;
            assertInstanceOf(Expr.Fun.class, outer.body());
        }

        @Test
        void testNestedPi() throws Exception {
            Expr expr = parseExpr("∀ x, ∀ y, x");
            assertInstanceOf(Expr.Pi.class, expr);
            Expr.Pi outer = (Expr.Pi) expr;
            assertInstanceOf(Expr.Pi.class, outer.body());
        }

        @Test
        void testAppInFunBody() throws Exception {
            Expr expr = parseExpr("fun f => f x");
            assertInstanceOf(Expr.Fun.class, expr);
            Expr.Fun fun = (Expr.Fun) expr;
            assertInstanceOf(Expr.App.class, fun.body());
        }

        @Test
        void testArrowWithApp() throws Exception {
            Expr expr = parseExpr("f x -> g y");
            assertInstanceOf(Expr.Arrow.class, expr);
            Expr.Arrow arrow = (Expr.Arrow) expr;
            assertInstanceOf(Expr.App.class, arrow.from());
            assertInstanceOf(Expr.App.class, arrow.to());
        }

        @Test
        void testComplexType() throws Exception {
            Expr expr = parseExpr("∀ (A : *), A -> A");
            assertInstanceOf(Expr.Pi.class, expr);
            Expr.Pi pi = (Expr.Pi) expr;
            assertInstanceOf(Expr.Arrow.class, pi.body());
        }
    }

    @Nested
    class EOITests {
        @Test
        void testEmptyInput() throws Exception {
            Command cmd = parseCommand("");
            assertNull(cmd);
        }

        @Test
        void testWhitespaceOnly() throws Exception {
            Command cmd = parseCommand("   \n\t  ");
            assertNull(cmd);
        }
    }
}

