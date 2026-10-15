package io.github.seraphina.infinity_item_editor_re.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    public static Component fromEditorText(String text) {
        MutableComponent result = Component.empty();
        StringBuilder segment = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current != ChatFormatting.PREFIX_CODE) {
                segment.append(current);
                continue;
            }

            if (i + 7 < text.length() && text.charAt(i + 1) == '#') {
                int color = 0;
                boolean valid = true;
                for (int j = 0; j < 6; j++) {
                    int digit = Character.digit(text.charAt(i + 2 + j), 16);
                    if (digit < 0) {
                        valid = false;
                        break;
                    }
                    color = color * 16 + digit;
                }
                if (valid) {
                    if (!segment.isEmpty()) {
                        result.append(Component.literal(segment.toString()).setStyle(style));
                        segment.setLength(0);
                    }
                    style = style.withColor(TextColor.fromRgb(color));
                    i += 7;
                    continue;
                }
            }

            if (i + 1 < text.length()) {
                ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(i + 1));
                if (formatting != null) {
                    if (!segment.isEmpty()) {
                        result.append(Component.literal(segment.toString()).setStyle(style));
                        segment.setLength(0);
                    }
                    style = style.applyLegacyFormat(formatting);
                    i++;
                    continue;
                }
            }

            segment.append(current);
        }
        if (!segment.isEmpty()) {
            result.append(Component.literal(segment.toString()).setStyle(style));
        }
        return result;
    }

    public static String toEditorText(Component component) {
        StringBuilder result = new StringBuilder();
        component.visit((style, text) -> {
            if (!style.isEmpty()) {
                result.append(ChatFormatting.RESET);
                TextColor color = style.getColor();
                if (color != null) {
                    ChatFormatting legacy = null;
                    for (ChatFormatting formatting : ChatFormatting.values()) {
                        TextColor legacyColor = TextColor.fromLegacyFormat(formatting);
                        if (legacyColor != null && legacyColor.equals(color)) {
                            legacy = formatting;
                            break;
                        }
                    }
                    if (legacy == null) {
                        result.append(String.format(Locale.ROOT, "§#%06X", color.getValue() & 0xFFFFFF));
                    } else {
                        result.append(legacy);
                    }
                }
                if (style.isObfuscated()) {
                    result.append(ChatFormatting.OBFUSCATED);
                }
                if (style.isBold()) {
                    result.append(ChatFormatting.BOLD);
                }
                if (style.isStrikethrough()) {
                    result.append(ChatFormatting.STRIKETHROUGH);
                }
                if (style.isUnderlined()) {
                    result.append(ChatFormatting.UNDERLINE);
                }
                if (style.isItalic()) {
                    result.append(ChatFormatting.ITALIC);
                }
            }
            result.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        return result.toString();
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
