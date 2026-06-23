package io.github.seraphina.infinity_item_editor_re.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class ComponentCompat {
    private ComponentCompat() {
    }

    public static String toJson(Component component) {
        return ComponentSerialization.CODEC
                .encodeStart(ItemStackNbt.provider().createSerializationContext(JsonOps.INSTANCE), component)
                .getOrThrow()
                .toString();
    }

    public static Component fromJson(String json) {
        return fromJson(JsonParser.parseString(json));
    }

    public static Component fromJson(JsonElement json) {
        return ComponentSerialization.CODEC
                .parse(ItemStackNbt.provider().createSerializationContext(JsonOps.INSTANCE), json)
                .getOrThrow(JsonSyntaxException::new);
    }

    public static Component fromJsonLenient(String json) {
        return fromJson(json);
    }
}
