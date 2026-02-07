package club.doki7.pl12.syntax;

public sealed interface Node permits Program, Command, Expr, ParamGroup, Argument {}
