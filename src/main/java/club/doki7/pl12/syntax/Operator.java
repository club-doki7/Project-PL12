package club.doki7.pl12.syntax;

import java.util.List;

public sealed interface Operator {
    enum Assoc { LEFT, RIGHT }

    record Infix(String lexeme, int prec, Assoc assoc) implements Operator {}

    record Mixfix(List<String> parts) implements Operator {
        public Mixfix {
            assert !parts.isEmpty();
        }
    }
}
