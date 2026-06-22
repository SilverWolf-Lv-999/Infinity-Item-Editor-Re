package io.github.seraphina.infinity_item_editor_re.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.util.Optional;
import java.util.UUID;

public final class NbtCompat {
    public static final int TAG_ANY_NUMERIC = 99;

    private NbtCompat() {
    }

    public static boolean contains(CompoundTag tag, String key, int type) {
        if (tag == null) {
            return false;
        }
        Tag value = tag.get(key);
        if (value == null) {
            return false;
        }
        return type == TAG_ANY_NUMERIC ? value instanceof NumericTag : value.getId() == type;
    }

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        return tag == null ? new CompoundTag() : tag.getCompoundOrEmpty(key);
    }

    public static ListTag getList(CompoundTag tag, String key, int ignoredType) {
        return tag == null ? new ListTag() : tag.getListOrEmpty(key);
    }

    public static byte getByte(CompoundTag tag, String key) {
        return tag == null ? 0 : tag.getByteOr(key, (byte) 0);
    }

    public static short getShort(CompoundTag tag, String key) {
        return tag == null ? 0 : tag.getShortOr(key, (short) 0);
    }

    public static int getInt(CompoundTag tag, String key) {
        return tag == null ? 0 : tag.getIntOr(key, 0);
    }

    public static long getLong(CompoundTag tag, String key) {
        return tag == null ? 0L : tag.getLongOr(key, 0L);
    }

    public static float getFloat(CompoundTag tag, String key) {
        return tag == null ? 0.0F : tag.getFloatOr(key, 0.0F);
    }

    public static double getDouble(CompoundTag tag, String key) {
        return tag == null ? 0.0D : tag.getDoubleOr(key, 0.0D);
    }

    public static String getString(CompoundTag tag, String key) {
        return tag == null ? "" : tag.getStringOr(key, "");
    }

    public static boolean getBoolean(CompoundTag tag, String key) {
        return tag != null && tag.getBooleanOr(key, false);
    }

    public static byte[] getByteArray(CompoundTag tag, String key) {
        return tag == null ? new byte[0] : tag.getByteArray(key).orElseGet(() -> new byte[0]);
    }

    public static int[] getIntArray(CompoundTag tag, String key) {
        return tag == null ? new int[0] : tag.getIntArray(key).orElseGet(() -> new int[0]);
    }

    public static long[] getLongArray(CompoundTag tag, String key) {
        return tag == null ? new long[0] : tag.getLongArray(key).orElseGet(() -> new long[0]);
    }

    public static CompoundTag getCompound(ListTag list, int index) {
        return list == null ? new CompoundTag() : list.getCompoundOrEmpty(index);
    }

    public static ListTag getList(ListTag list, int index) {
        return list == null ? new ListTag() : list.getListOrEmpty(index);
    }

    public static short getShort(ListTag list, int index) {
        return list == null ? 0 : list.getShortOr(index, (short) 0);
    }

    public static int getInt(ListTag list, int index) {
        return list == null ? 0 : list.getIntOr(index, 0);
    }

    public static float getFloat(ListTag list, int index) {
        return list == null ? 0.0F : list.getFloatOr(index, 0.0F);
    }

    public static double getDouble(ListTag list, int index) {
        return list == null ? 0.0D : list.getDoubleOr(index, 0.0D);
    }

    public static String getString(ListTag list, int index) {
        return list == null ? "" : list.getStringOr(index, "");
    }

    public static int[] getIntArray(ListTag list, int index) {
        return list == null ? new int[0] : list.getIntArray(index).orElseGet(() -> new int[0]);
    }

    public static long[] getLongArray(ListTag list, int index) {
        return list == null ? new long[0] : list.getLongArray(index).orElseGet(() -> new long[0]);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag != null && tag.getIntArray(key).map(values -> values.length == 4).orElse(false);
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        int[] values = getIntArray(tag, key);
        if (values.length != 4) {
            return new UUID(0L, 0L);
        }
        long most = ((long) values[0] << 32) | (values[1] & 0xFFFFFFFFL);
        long least = ((long) values[2] << 32) | (values[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        tag.putIntArray(key, new int[]{
                (int) (most >> 32),
                (int) most,
                (int) (least >> 32),
                (int) least
        });
    }

    public static CompoundTag parseTag(String value) throws CommandSyntaxException {
        return TagParser.parseCompoundFully(value);
    }

    public static String asString(Tag tag) {
        return tag instanceof StringTag stringTag ? stringTag.value() : "";
    }

    public static Number asNumber(Tag tag) {
        return tag instanceof NumericTag numericTag ? numericTag.box() : 0;
    }

    public static int[] asIntArray(Tag tag) {
        return tag instanceof IntArrayTag arrayTag ? arrayTag.getAsIntArray() : new int[0];
    }

    public static long[] asLongArray(Tag tag) {
        return tag instanceof LongArrayTag arrayTag ? arrayTag.getAsLongArray() : new long[0];
    }

    public static byte[] asByteArray(Tag tag) {
        return tag instanceof ByteArrayTag arrayTag ? arrayTag.getAsByteArray() : new byte[0];
    }

    public static <T> T or(Optional<T> value, T fallback) {
        return value.orElse(fallback);
    }
}
