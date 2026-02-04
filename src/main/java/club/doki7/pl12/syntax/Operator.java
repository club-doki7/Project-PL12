package club.doki7.pl12.syntax;

public sealed interface Operator {
    enum Assoc { LEFT, RIGHT, NONE }

    record Prefix(String lexeme) implements Operator {}

    record Infix(String lexeme, int prec, Assoc assoc) implements Operator {}
}
