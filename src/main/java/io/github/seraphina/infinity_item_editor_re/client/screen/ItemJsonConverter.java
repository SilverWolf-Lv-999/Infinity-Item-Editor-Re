package io.github.seraphina.infinity_item_editor_re.client.screen;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ItemJsonConverter {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Set<String> STACK_ROOT_KEYS = Set.of("id", "item", "Item", "Count", "count", "components", "tag", "nbt");

    private ItemJsonConverter() {
    }

    static String toJson(ItemStack stack) {
        CompoundTag saved = ItemStackNbt.save(stack);
        return PRETTY_GSON.toJson(toJsonElement(saved));
    }

    static String format(String value) throws JsonParseException {
        JsonElement parsed = JsonParser.parseString(value);
        return PRETTY_GSON.toJson(parsed);
    }

    static ItemStack fromJson(String value) throws JsonParseException {
        JsonElement parsed = JsonParser.parseString(value);
        if (!parsed.isJsonObject()) {
            throw new JsonParseException("Root must be a JSON object.");
        }

        CompoundTag saved = jsonObjectToCompound(parsed.getAsJsonObject());
        normalizeStackKeys(saved);
        ItemStack stack = ItemStackNbt.parse(saved);
        if (stack.isEmpty() && !"minecraft:air".equals(saved.getString("id"))) {
            throw new JsonParseException("JSON does not describe a valid item stack.");
        }
        return stack;
    }

    private static void normalizeStackKeys(CompoundTag saved) {
        if (!saved.contains("id")) {
            if (saved.contains("item")) {
                saved.putString("id", saved.getString("item"));
            } else if (saved.contains("Item")) {
                saved.putString("id", saved.getString("Item"));
            }
        }
        if (!saved.contains("Count") && saved.contains("count")) {
            saved.putByte("Count", saved.getByte("count"));
        }
        if (!saved.contains("Count")) {
            saved.putByte("Count", (byte) 1);
        }
        if (!saved.contains("tag") && saved.get("nbt") instanceof CompoundTag nbt) {
            saved.put("tag", nbt.copy());
        }
        moveLooseRootTags(saved);
    }

    private static void moveLooseRootTags(CompoundTag saved) {
        CompoundTag itemTag = saved.get("tag") instanceof CompoundTag existing ? existing.copy() : new CompoundTag();
        for (String key : new ArrayList<>(saved.getAllKeys())) {
            if (STACK_ROOT_KEYS.contains(key) || itemTag.contains(key)) {
                continue;
            }
            Tag value = saved.get(key);
            if (value != null) {
                itemTag.put(key, value.copy());
            }
        }
        if (!itemTag.isEmpty()) {
            saved.put("tag", itemTag);
        }
    }

    private static CompoundTag jsonObjectToCompound(JsonObject object) {
        CompoundTag compound = new CompoundTag();
        for (String key : object.keySet()) {
            Tag tag = toTag(key, object.get(key));
            if (tag != null) {
                compound.put(key, tag);
            }
        }
        return compound;
    }

    private static Tag toTag(String key, JsonElement element) {
        if (element == null || element instanceof JsonNull || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return jsonObjectToCompound(element.getAsJsonObject());
        }
        if (element.isJsonArray()) {
            return toArrayTag(key, element.getAsJsonArray());
        }
        if (!element.isJsonPrimitive()) {
            return StringTag.valueOf(element.toString());
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return ByteTag.valueOf(primitive.getAsBoolean());
        }
        if (primitive.isString()) {
            return StringTag.valueOf(primitive.getAsString());
        }
        if (primitive.isNumber()) {
            return toNumberTag(key, primitive);
        }
        return StringTag.valueOf(primitive.getAsString());
    }

    private static Tag toArrayTag(String key, JsonArray array) {
        if (array.isEmpty()) {
            return new ListTag();
        }
        if (isByteArrayKey(key) && allIntegralNumbers(array)) {
            byte[] values = new byte[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsByte();
            }
            return new ByteArrayTag(values);
        }
        if (isIntArrayKey(key) && allIntegralNumbers(array)) {
            List<Integer> values = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                values.add(element.getAsInt());
            }
            return new IntArrayTag(values);
        }
        if (isLongArrayKey(key) && allIntegralNumbers(array)) {
            long[] values = new long[array.size()];
            for (int i = 0; i < array.size(); i++) {
                values[i] = array.get(i).getAsLong();
            }
            return new LongArrayTag(values);
        }

        ListTag list = new ListTag();
        for (JsonElement element : array) {
            Tag tag = toTag(key, element);
            if (tag != null) {
                list.add(tag);
            }
        }
        return list;
    }

    private static Tag toNumberTag(String key, JsonPrimitive primitive) {
        String raw = primitive.getAsString();
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if (raw.indexOf('.') >= 0 || raw.indexOf('e') >= 0 || raw.indexOf('E') >= 0) {
            if (normalizedKey.contains("multiplier") || normalizedKey.equals("amount")) {
                return FloatTag.valueOf(primitive.getAsFloat());
            }
            return DoubleTag.valueOf(primitive.getAsDouble());
        }

        long value = primitive.getAsLong();
        if (normalizedKey.equals("count") || normalizedKey.equals("slot") || normalizedKey.equals("type")
                || normalizedKey.equals("flight") || normalizedKey.equals("operation")) {
            return ByteTag.valueOf((byte) value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE && normalizedKey.endsWith("short")) {
            return ShortTag.valueOf((short) value);
        }
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            return IntTag.valueOf((int) value);
        }
        return LongTag.valueOf(value);
    }

    private static boolean allIntegralNumbers(JsonArray array) {
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                return false;
            }
            String raw = element.getAsString();
            if (raw.indexOf('.') >= 0 || raw.indexOf('e') >= 0 || raw.indexOf('E') >= 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isByteArrayKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.endsWith("bytes") || normalized.endsWith("bytearray");
    }

    private static boolean isIntArrayKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.equals("id") || normalized.equals("uuid") || normalized.endsWith("uuid")
                || normalized.equals("colors") || normalized.equals("fadecolors");
    }

    private static boolean isLongArrayKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.endsWith("longs") || normalized.endsWith("longarray");
    }

    private static JsonElement toJsonElement(Tag tag) {
        if (tag == null) {
            return JsonNull.INSTANCE;
        }
        if (tag instanceof CompoundTag compoundTag) {
            JsonObject object = new JsonObject();
            for (String key : compoundTag.getAllKeys()) {
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
            return new JsonPrimitive(stringTag.getAsString());
        }
        if (tag instanceof NumericTag numericTag) {
            return new JsonPrimitive(numericTag.getAsNumber());
        }
        return new JsonPrimitive(tag.toString());
    }
}
