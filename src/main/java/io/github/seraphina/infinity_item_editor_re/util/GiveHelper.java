package io.github.seraphina.infinity_item_editor_re.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import io.github.seraphina.infinity_item_editor_re.util.CompatRegistries;

public final class GiveHelper {
    private GiveHelper() {
    }

    public static String getStringFromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        Identifier itemName = CompatRegistries.ITEMS.getKey(stack.getItem());
        StringBuilder command = new StringBuilder("/give @p ");
        command.append(itemName == null ? "minecraft:air" : itemName);

        CompoundTag tag = ItemStackNbt.get(stack);
        if (tag != null && !tag.isEmpty()) {
            command.append(tag);
        }

        if (stack.getCount() != 1) {
            command.append(' ').append(stack.getCount());
        }

        return command.toString();
    }

    public static ItemStack getItemStackFromString(String command, HolderLookup.Provider lookupProvider) {
        if (command == null || command.isBlank() || lookupProvider == null) {
            return ItemStack.EMPTY;
        }

        try {
            String itemArgument = stripGiveCommand(command.trim());
            if (itemArgument.isBlank()) {
                return ItemStack.EMPTY;
            }

            StringReader reader = new StringReader(itemArgument);
            ItemParser.ItemResult itemResult = new ItemParser(lookupProvider).parse(reader);
            int count = 1;
            reader.skipWhitespace();
            if (reader.canRead()) {
                count = reader.readInt();
            }
            if (count <= 0) {
                return ItemStack.EMPTY;
            }

            return new ItemInput(itemResult.item(), itemResult.components()).createItemStack(count, false);
        } catch (CommandSyntaxException exception) {
            return ItemStack.EMPTY;
        }
    }

    private static String stripGiveCommand(String command) throws CommandSyntaxException {
        StringReader reader = new StringReader(command);
        reader.skipWhitespace();
        String literal = reader.readUnquotedString();
        if (!"give".equals(literal) && !"/give".equals(literal)) {
            return command;
        }

        reader.skipWhitespace();
        if (!reader.canRead()) {
            return "";
        }
        reader.readUnquotedString();
        reader.skipWhitespace();
        return reader.canRead() ? command.substring(reader.getCursor()) : "";
    }
}
