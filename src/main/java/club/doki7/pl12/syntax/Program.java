package club.doki7.pl12.syntax;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/// ```bnf
/// program ::= command*
/// ```
public record Program(@NotNull List<@NotNull Command> commands) implements Node {
}
