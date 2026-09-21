package io.github.seraphina.infinity_item_editor_re.data.realms;

import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.core.HolderLookup;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RealmController {
    public static final String VERSION = "0.2";

    private final File dataFile;
    private final NonNullList<ItemStack> stackList = NonNullList.create();
    private final List<CompoundTag> unresolvedStackTags = new ArrayList<>();
    private HolderLookup.Provider loadedProvider;

    public RealmController(File dataDir) {
        this.dataFile = new File(dataDir, "realm.nbt");
        read();
    }

    public void read() {
        read(ItemStackNbt.provider());
    }

    public synchronized void read(HolderLookup.Provider provider) {

        if (!dataFile.exists()) {
            stackList.clear();
            unresolvedStackTags.clear();
            loadedProvider = provider;
            return;
        }

        try {
            CompoundTag root = readRootTag();
            if (root == null || !NbtCompat.contains(root, "realm", Tag.TAG_LIST)) {
                return;
            }

            NonNullList<ItemStack> loadedStacks = NonNullList.create();
            List<CompoundTag> unresolvedTags = new ArrayList<>();
            ListTag realm = NbtCompat.getList(root, "realm", Tag.TAG_COMPOUND);
            for (Tag tag : realm) {
                if (tag instanceof CompoundTag stackTag) {
                    decodeOrKeep(stackTag, provider, loadedStacks, unresolvedTags);
                }
            }

            stackList.clear();
            stackList.addAll(loadedStacks);
            unresolvedStackTags.clear();
            unresolvedStackTags.addAll(unresolvedTags);
            loadedProvider = provider;
        } catch (Exception exception) {
            ModSource.LOGGER.error("Failed to load infinity realm from {}", dataFile.getAbsolutePath(), exception);
        }
    }

    public synchronized void write() {
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
            for (CompoundTag unresolvedStackTag : unresolvedStackTags) {
                realm.add(unresolvedStackTag.copy());
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

        refreshForProvider(player.level().registryAccess());
        resolveUnresolvedStacks(player.level().registryAccess());
        ItemStack savedStack = stack.copy();
        for (ItemStack existingStack : stackList) {
            if (ItemStack.matches(existingStack, savedStack)) {
                player.sendSystemMessage(Component.literal("Didn't add ")
                        .append(savedStack.getHoverName())
                        .append(Component.literal(", as it seems to already exist in the Infinity Realm.")));
                return false;
            }
        }

        stackList.add(savedStack);
        write();
        player.sendSystemMessage(Component.literal("Added ")
                .append(savedStack.getHoverName())
                .append(Component.literal(" to Infinity Realm.")));
        return true;
    }

    public boolean removeItemStack(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        refreshForProvider(player.level().registryAccess());
        resolveUnresolvedStacks(player.level().registryAccess());
        for (ItemStack existingStack : stackList) {
            if (ItemStack.matches(existingStack, stack)) {
                stackList.remove(existingStack);
                write();
                player.sendSystemMessage(Component.literal("Banished ")
                        .append(stack.getHoverName())
                        .append(Component.literal(" from the Infinity Realm.")));
                return true;
            }
        }
        return false;
    }

    public synchronized List<ItemStack> getStackList() {
        return getStackList(ItemStackNbt.provider());
    }

    public synchronized List<ItemStack> getStackList(HolderLookup.Provider provider) {
        refreshForProvider(provider);
        resolveUnresolvedStacks(provider);
        return Collections.unmodifiableList(stackList);
    }

    private void refreshForProvider(HolderLookup.Provider provider) {
        if (provider != null && provider != loadedProvider) {
            read(provider);
        }
    }

    private void resolveUnresolvedStacks(HolderLookup.Provider provider) {
        Iterator<CompoundTag> iterator = unresolvedStackTags.iterator();
        while (iterator.hasNext()) {
            CompoundTag stackTag = iterator.next();
            try {
                ItemStack stack = ItemStackNbt.parse(stackTag, provider);
                if (!stack.isEmpty()) {
                    stackList.add(stack);
                    iterator.remove();
                }
            } catch (Exception exception) {
                ModSource.LOGGER.debug("Could not resolve an infinity realm item yet", exception);
            }
        }
    }

    private void decodeOrKeep(CompoundTag stackTag, HolderLookup.Provider provider,
                              List<ItemStack> loadedStacks, List<CompoundTag> unresolvedTags) {
        try {
            ItemStack stack = ItemStackNbt.parse(stackTag, provider);
            if (!stack.isEmpty()) {
                loadedStacks.add(stack);
                return;
            }
        } catch (Exception exception) {
            ModSource.LOGGER.debug("Could not decode an infinity realm item yet", exception);
        }
        unresolvedTags.add(stackTag.copy());
    }
}
