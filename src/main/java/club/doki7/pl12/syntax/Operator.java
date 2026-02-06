package club.doki7.pl12.syntax;

public record Operator(String lexeme, int prec, Assoc assoc) {
    enum Assoc { LEFT, RIGHT, NONE }
}
