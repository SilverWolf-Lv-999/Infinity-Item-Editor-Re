package io.github.seraphina.infinity_item_editor_re.data.realms;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class RealmController {
    public static final String VERSION = "0.2";

    private final File dataFile;
    private final NonNullList<ItemStack> stackList = NonNullList.create();

    public RealmController(File dataDir) {
        this.dataFile = new File(dataDir, "realm.nbt");
        read();
    }

    public void read() {
        stackList.clear();

        if (!dataFile.exists()) {
            return;
        }

        try {
            CompoundTag root = readRootTag();
            if (root == null || !root.contains("realm", Tag.TAG_LIST)) {
                return;
            }

            ListTag realm = root.getList("realm", Tag.TAG_COMPOUND);
            for (Tag tag : realm) {
                if (tag instanceof CompoundTag stackTag) {
                    ItemStack stack = ItemStackNbt.parse(stackTag);
                    if (!stack.isEmpty()) {
                        stackList.add(stack);
                    }
                }
            }
        } catch (Exception exception) {
            ModSource.LOGGER.error("Failed to load infinity realm from {}", dataFile.getAbsolutePath(), exception);
        }
    }

    public void write() {
        try {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                ModSource.LOGGER.warn("Failed to create realm data directory {}", parent.getAbsolutePath());
            }

            CompoundTag root = new CompoundTag();
            ListTag realm = new ListTag();
            root.put("realm", realm);
            root.putString("realm_version", VERSION);

            for (ItemStack itemStack : stackList) {
                realm.add(ItemStackNbt.save(itemStack));
            }

            NbtIo.writeCompressed(root, dataFile.toPath());
        } catch (Exception exception) {
            ModSource.LOGGER.error("Failed to save infinity realm to {}", dataFile.getAbsolutePath(), exception);
        }
    }

    private CompoundTag readRootTag() throws IOException {
        try {
            return NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
        } catch (IOException compressedException) {
            try {
                return NbtIo.read(dataFile.toPath());
            } catch (IOException uncompressedException) {
                compressedException.addSuppressed(uncompressedException);
                throw compressedException;
            }
        }
    }

    public boolean addItemStack(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ItemStack savedStack = stack.copy();
        for (ItemStack existingStack : stackList) {
            if (ItemStack.matches(existingStack, savedStack)) {
                player.displayClientMessage(Component.literal("Didn't add ")
                        .append(savedStack.getHoverName())
                        .append(Component.literal(", as it seems to already exist in the Infinity Realm.")), false);
                return false;
            }
        }

        stackList.add(savedStack);
        write();
        player.displayClientMessage(Component.literal("Added ")
                .append(savedStack.getHoverName())
                .append(Component.literal(" to Infinity Realm.")), false);
        return true;
    }

    public boolean removeItemStack(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        for (ItemStack existingStack : stackList) {
            if (ItemStack.matches(existingStack, stack)) {
                stackList.remove(existingStack);
                write();
                player.displayClientMessage(Component.literal("Banished ")
                        .append(stack.getHoverName())
                        .append(Component.literal(" from the Infinity Realm.")), false);
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> getStackList() {
        return Collections.unmodifiableList(stackList);
    }
}
