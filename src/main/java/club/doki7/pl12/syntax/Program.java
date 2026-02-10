package club.doki7.pl12.syntax;

import club.doki7.pl12.util.ImmSeq;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/// ```bnf
/// program ::= command*
/// ```
public record Program(@NotNull ImmSeq<@NotNull Command> commands) implements Node {
    public static Program of(List<Command> commands) {
        return new Program(ImmSeq.of(commands));
    }
}
