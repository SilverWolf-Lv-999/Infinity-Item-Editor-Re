package io.github.seraphina.infinity_item_editor_re.platform;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class PlatformServices {
    private static Function<String, Optional<String>> modDisplayNameResolver = namespace -> Optional.empty();

    private PlatformServices() {
    }

    public static void setModDisplayNameResolver(Function<String, Optional<String>> resolver) {
        modDisplayNameResolver = Objects.requireNonNull(resolver);
    }

    public static Optional<String> getModDisplayName(String namespace) {
        return modDisplayNameResolver.apply(namespace);
    }
}
