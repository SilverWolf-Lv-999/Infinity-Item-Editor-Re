package io.github.seraphina.infinity_item_editor_re.util;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;

public final class ComponentCompat {
    private ComponentCompat() {
    }

    public static String toJson(Component component) {
        return Component.Serializer.toJson(component, ItemStackNbt.provider());
    }

    public static Component fromJson(String json) {
        return Component.Serializer.fromJson(json, ItemStackNbt.provider());
    }

    public static Component fromJson(JsonElement json) {
        return Component.Serializer.fromJson(json, ItemStackNbt.provider());
    }

    public static Component fromJsonLenient(String json) {
        return Component.Serializer.fromJsonLenient(json, ItemStackNbt.provider());
    }
}
