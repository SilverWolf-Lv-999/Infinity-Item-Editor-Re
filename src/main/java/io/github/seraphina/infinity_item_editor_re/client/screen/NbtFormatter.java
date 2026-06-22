package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class NbtFormatter {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private NbtFormatter() {
    }

    static List<Component> prettyLines(Tag tag) {
        List<Component> lines = new ArrayList<>();
        for (String line : prettyJson(tag).split("\\n")) {
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + line));
        }
        return lines;
    }

    static List<NbtRow> rows(CompoundTag tag, Set<String> expandedPaths) {
        List<NbtRow> rows = new ArrayList<>();
        if (tag == null || tag.isEmpty()) {
            rows.add(new NbtRow("tag", "tag: {}", false, 0));
            return rows;
        }

        addRows(rows, expandedPaths, "tag", "tag", tag, 0);
        return rows;
    }

    private static void addRows(List<NbtRow> rows, Set<String> expandedPaths, String path, String name, Tag tag, int depth) {
        boolean expandable = tag instanceof CompoundTag || tag instanceof ListTag;
        String prefix = expandable ? (expandedPaths.contains(path) ? "- " : "+ ") : "  ";
        rows.add(new NbtRow(path, prefix + name + ": " + summarizeTag(tag), expandable, depth));
        if (!expandable || !expandedPaths.contains(path)) {
            return;
        }

        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.keySet()) {
                addRows(rows, expandedPaths, path + "." + key, key, compoundTag.get(key), depth + 1);
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                addRows(rows, expandedPaths, path + "[" + i + "]", "[" + i + "]", listTag.get(i), depth + 1);
            }
        }
    }

    private static String summarizeTag(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return "{" + compoundTag.keySet().size() + "}";
        }
        if (tag instanceof ListTag listTag) {
            return "[" + listTag.size() + "]";
        }
        String value = tag.toString();
        return value.length() > 120 ? value.substring(0, 117) + "..." : value;
    }

    private static String prettyJson(Tag tag) {
        if (tag == null || tag instanceof CompoundTag compoundTag && compoundTag.isEmpty()) {
            return "{}";
        }

        String raw = tag.toString();
        try {
            return PRETTY_GSON.toJson(JsonParser.parseString(raw));
        } catch (JsonParseException ignored) {
            return PRETTY_GSON.toJson(toJsonElement(tag));
        }
    }

    private static JsonElement toJsonElement(Tag tag) {
        if (tag == null) {
            return JsonNull.INSTANCE;
        }
        if (tag instanceof CompoundTag compoundTag) {
            JsonObject object = new JsonObject();
            for (String key : compoundTag.keySet()) {
                object.add(key, toJsonElement(compoundTag.get(key)));
            }
            return object;
        }
        if (tag instanceof ListTag listTag) {
            JsonArray array = new JsonArray();
            for (int i = 0; i < listTag.size(); i++) {
                array.add(toJsonElement(listTag.get(i)));
            }
            return array;
        }
        if (tag instanceof ByteArrayTag byteArrayTag) {
            JsonArray array = new JsonArray();
            for (byte value : byteArrayTag.getAsByteArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof IntArrayTag intArrayTag) {
            JsonArray array = new JsonArray();
            for (int value : intArrayTag.getAsIntArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof LongArrayTag longArrayTag) {
            JsonArray array = new JsonArray();
            for (long value : longArrayTag.getAsLongArray()) {
                array.add(value);
            }
            return array;
        }
        if (tag instanceof StringTag stringTag) {
            return new JsonPrimitive(stringTag.value());
        }
        if (tag instanceof NumericTag numericTag) {
            return new JsonPrimitive(numericTag.box());
        }
        return new JsonPrimitive(tag.toString());
    }
}
