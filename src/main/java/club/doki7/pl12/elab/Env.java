package club.doki7.pl12.elab;

import club.doki7.pl12.core.Type;
import club.doki7.pl12.core.Value;
import club.doki7.pl12.syntax.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Env {
    public record Entry(@NotNull String name,
                        @NotNull Type type,
                        @NotNull Value value,
                        @NotNull Token location) {}

    public record Module(@NotNull String name,
                         @NotNull Env env,
                         @NotNull Token location) {}

    public final @Nullable Env parent;

    public @Nullable Entry lookup(@NotNull String name) {
        Env current = this;
        while (current != null) {
            Env.Entry entry = current.entries.get(name);
            if (entry != null) {
                return entry;
            }
            current = current.parent;
        }
        return null;
    }

    public @Nullable Module getModule(@NotNull String name) {
        return modules.get(name);
    }

    private Env(@Nullable Env parent) {
        this.parent = parent;
    }

    private final HashMap<String, Entry> entries = new HashMap<>();
    private final HashMap<String, Module> modules = new HashMap<>();
}
