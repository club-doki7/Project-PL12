package club.doki7.pl12.syntax;

import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

/// ```bnf
/// program ::= command*
/// ```
public record Program(@NotNull ImmSeq<@NotNull Command> commands) implements Node {
}
