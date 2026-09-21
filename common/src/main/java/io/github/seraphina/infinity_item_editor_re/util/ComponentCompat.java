package io.github.seraphina.infinity_item_editor_re.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;

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

    public static ListTag toNbtList(List<Component> components) {
        Tag encoded = ComponentSerialization.CODEC
                .listOf()
                .encodeStart(ItemStackNbt.provider().createSerializationContext(NbtOps.INSTANCE), components)
                .getOrThrow();
        if (encoded instanceof ListTag list) {
            return list;
        }
        throw new IllegalStateException("Component list codec did not produce an NBT list");
    }

    public static List<Component> fromNbtList(Tag tag) {
        return ComponentSerialization.CODEC
                .listOf()
                .parse(ItemStackNbt.provider().createSerializationContext(NbtOps.INSTANCE), tag)
                .getOrThrow(JsonSyntaxException::new);
    }
}
