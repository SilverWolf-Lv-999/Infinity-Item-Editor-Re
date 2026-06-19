package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class NbtFormatter {
    private NbtFormatter() {
    }

    static List<Component> prettyLines(Tag tag) {
        if (tag == null || tag instanceof CompoundTag compoundTag && compoundTag.isEmpty()) {
            return List.of(Component.literal(ChatFormatting.DARK_PURPLE + "{}"));
        }

        List<Component> lines = new ArrayList<>();
        appendPrettyTagLines(lines, tag, 0, "");
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

    private static void appendPrettyTagLines(List<Component> lines, Tag tag, int depth, String name) {
        String indent = "  ".repeat(depth);
        if (tag instanceof CompoundTag compoundTag) {
            if (!name.isEmpty()) {
                lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": {"));
            } else {
                lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "{"));
            }
            for (String key : compoundTag.getAllKeys()) {
                appendPrettyTagLines(lines, compoundTag.get(key), depth + 1, key);
            }
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "}"));
        } else if (tag instanceof ListTag listTag) {
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": ["));
            for (int i = 0; i < listTag.size(); i++) {
                appendPrettyTagLines(lines, listTag.get(i), depth + 1, Integer.toString(i));
            }
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + "]"));
        } else {
            lines.add(Component.literal(ChatFormatting.DARK_PURPLE + indent + name + ": " + tag));
        }
    }

    private static void addRows(List<NbtRow> rows, Set<String> expandedPaths, String path, String name, Tag tag, int depth) {
        boolean expandable = tag instanceof CompoundTag || tag instanceof ListTag;
        String prefix = expandable ? (expandedPaths.contains(path) ? "- " : "+ ") : "  ";
        rows.add(new NbtRow(path, prefix + name + ": " + summarizeTag(tag), expandable, depth));
        if (!expandable || !expandedPaths.contains(path)) {
            return;
        }

        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
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
            return "{" + compoundTag.getAllKeys().size() + "}";
        }
        if (tag instanceof ListTag listTag) {
            return "[" + listTag.size() + "]";
        }
        String value = tag.toString();
        return value.length() > 120 ? value.substring(0, 117) + "..." : value;
    }
}
