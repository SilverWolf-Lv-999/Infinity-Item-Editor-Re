package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackCompat;

import io.github.seraphina.infinity_item_editor_re.util.ComponentCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import io.github.seraphina.infinity_item_editor_re.client.screen.BannerPatternCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ClientCreativeTabData {
    private static final int HOTBAR_SIZE = 9;
    private static final int MAX_CHAT_LINKED_ITEMS = 256;
    private static final FireworkExplosion.Shape[] FIREWORK_SHAPES = {
            FireworkExplosion.Shape.SMALL_BALL,
            FireworkExplosion.Shape.LARGE_BALL,
            FireworkExplosion.Shape.STAR,
            FireworkExplosion.Shape.CREEPER,
            FireworkExplosion.Shape.BURST
    };
    private static final List<ItemStack> CHAT_LINKED_ITEMS = new ArrayList<>();

    private ClientCreativeTabData() {
    }

    public static void addPlayerHeads(List<ItemStack> heads) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        addPlayerHead(heads, minecraft.player);
        for (Player player : minecraft.level.players()) {
            if (player != minecraft.player) {
                addPlayerHead(heads, player);
            }
        }
    }

    public static void addThiefItems(List<ItemStack> stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            addChatLinkedItems(stacks);
            return;
        }

        for (Player player : minecraft.level.players()) {
            if (player == minecraft.player || player.getUUID().equals(minecraft.player.getUUID())) {
                continue;
            }
            addPlayerEquipment(stacks, player);
        }

        addChatLinkedItems(stacks);
    }

    public static boolean rememberChatLinkedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        for (ItemStack existingStack : CHAT_LINKED_ITEMS) {
            if (ItemStackCompat.isSameItemSameTags(existingStack, copy)) {
                return false;
            }
        }

        CHAT_LINKED_ITEMS.add(0, copy);
        while (CHAT_LINKED_ITEMS.size() > MAX_CHAT_LINKED_ITEMS) {
            CHAT_LINKED_ITEMS.remove(CHAT_LINKED_ITEMS.size() - 1);
        }
        return true;
    }

    public static void addFireworkVariants(List<ItemStack> stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ItemStack currentStar = ItemStack.EMPTY;
        ItemStack currentRocket = ItemStack.EMPTY;
        int size = Math.min(HOTBAR_SIZE, minecraft.player.getInventory().items.size());
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = minecraft.player.getInventory().items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (currentStar.isEmpty() && stack.is(Items.FIREWORK_STAR)) {
                currentStar = stack;
            } else if (currentRocket.isEmpty() && stack.is(Items.FIREWORK_ROCKET)) {
                currentRocket = stack;
            }
        }

        addRocketFlightVariants(stacks, currentRocket);
        if (currentStar.isEmpty() || ItemStackNbt.getElement(currentStar, "Explosion") == null) {
            addDefaultFireworkStars(stacks);
            return;
        }

        addStarToggleVariants(stacks, currentStar);
        addRocketWithStar(stacks, currentRocket, currentStar);
    }

    public static void addBannerVariants(List<ItemStack> stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ItemStack currentBanner = ItemStack.EMPTY;
        int size = Math.min(HOTBAR_SIZE, minecraft.player.getInventory().items.size());
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = minecraft.player.getInventory().items.get(slot);
            if (isBannerEditable(stack)) {
                currentBanner = stack;
                break;
            }
        }

        if (currentBanner.isEmpty()) {
            addPlacedBannerItems(stacks, minecraft);
            return;
        }

        addBannerSwapVariant(stacks, currentBanner);
        addBannerPatternVariants(stacks, currentBanner);
    }

    private static void addPlayerHead(List<ItemStack> heads, Player player) {
        if (player == null || player.getGameProfile() == null) {
            return;
        }

        String owner = player.getGameProfile().getName();
        if (owner == null || owner.isBlank()) {
            return;
        }

        addUnique(heads, createPlayerHead(owner));
    }

    private static void addPlayerEquipment(List<ItemStack> stacks, Player player) {
        boolean addedNote = false;
        ItemStack note = createNote("Stolen from " + player.getName().getString());
        for (ItemStack stack : player.getHandSlots()) {
            addedNote = addStackWithNote(stacks, stack, note, addedNote);
        }
        for (ItemStack stack : player.getArmorSlots()) {
            addedNote = addStackWithNote(stacks, stack, note, addedNote);
        }
    }

    private static void addChatLinkedItems(List<ItemStack> stacks) {
        boolean addedNote = false;
        ItemStack note = createNote("Linked in chat", "Items linked in chat, such as death messages");
        for (ItemStack stack : CHAT_LINKED_ITEMS) {
            addedNote = addStackWithNote(stacks, stack, note, addedNote);
        }
    }

    private static boolean addStackWithNote(List<ItemStack> stacks, ItemStack stack, ItemStack note, boolean addedNote) {
        if (stack == null || stack.isEmpty() || containsStack(stacks, stack)) {
            return addedNote;
        }

        if (!addedNote) {
            addUnique(stacks, note);
            addedNote = true;
        }
        addUnique(stacks, stack);
        return true;
    }

    private static void addRocketFlightVariants(List<ItemStack> stacks, ItemStack currentRocket) {
        if (currentRocket.isEmpty() || ItemStackNbt.getElement(currentRocket, "Fireworks") == null) {
            return;
        }

        for (byte flight = 1; flight <= 4; flight++) {
            ItemStack rocket = currentRocket.copy();
            rocket.setCount(1);
            ItemStackNbt.getOrCreateElement(rocket, "Fireworks").putByte("Flight", flight);
            addUnique(stacks, rocket);
        }
    }

    private static void addDefaultFireworkStars(List<ItemStack> stacks) {
        for (byte type = 0; type < 5; type++) {
            for (DyeColor color : DyeColor.values()) {
                ItemStack star = new ItemStack(Items.FIREWORK_STAR);
                CompoundTag explosion = ItemStackNbt.getOrCreateElement(star, "Explosion");
                explosion.putByte("Type", (byte) getFireworkShape(type).getId());
                explosion.putIntArray("Colors", new int[]{color.getFireworkColor()});
                addUnique(stacks, star);
            }
        }
    }

    private static FireworkExplosion.Shape getFireworkShape(int type) {
        if (type < 0 || type >= FIREWORK_SHAPES.length) {
            return FireworkExplosion.Shape.SMALL_BALL;
        }
        return FIREWORK_SHAPES[type];
    }

    private static void addStarToggleVariants(List<ItemStack> stacks, ItemStack currentStar) {
        addStarToggleVariant(stacks, currentStar, "Flicker", false);
        addStarToggleVariant(stacks, currentStar, "Flicker", true);
        addStarToggleVariant(stacks, currentStar, "Trail", false);
        addStarToggleVariant(stacks, currentStar, "Trail", true);
    }

    private static void addStarToggleVariant(List<ItemStack> stacks, ItemStack currentStar, String key, boolean enabled) {
        ItemStack star = currentStar.copy();
        star.setCount(1);
        ItemStackNbt.getOrCreateElement(star, "Explosion").putBoolean(key, enabled);
        addUnique(stacks, star);
    }

    private static void addRocketWithStar(List<ItemStack> stacks, ItemStack currentRocket, ItemStack currentStar) {
        CompoundTag starExplosion = ItemStackNbt.getElement(currentStar, "Explosion");
        if (starExplosion == null) {
            return;
        }

        ItemStack rocket = currentRocket.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : currentRocket.copy();
        rocket.setCount(1);
        CompoundTag fireworks = ItemStackNbt.getOrCreateElement(rocket, "Fireworks");
        ListTag explosions = fireworks.contains("Explosions", Tag.TAG_LIST)
                ? fireworks.getList("Explosions", Tag.TAG_COMPOUND).copy()
                : new ListTag();

        if (explosions.isEmpty()) {
            explosions.add(starExplosion.copy());
        } else if (starExplosion.contains("Colors", Tag.TAG_INT_ARRAY)) {
            int[] fadeColors = starExplosion.getIntArray("Colors");
            for (int index = 0; index < explosions.size(); index++) {
                explosions.getCompound(index).putIntArray("FadeColors", fadeColors);
            }
        } else {
            explosions.add(starExplosion.copy());
        }

        fireworks.put("Explosions", explosions);
        addUnique(stacks, rocket);
    }

    private static void addPlacedBannerItems(List<ItemStack> stacks, Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        ChunkPos center = minecraft.player.chunkPosition();
        int radius = Math.max(2, minecraft.options.getEffectiveRenderDistance());
        for (int chunkZ = center.z - radius; chunkZ <= center.z + radius; chunkZ++) {
            for (int chunkX = center.x - radius; chunkX <= center.x + radius; chunkX++) {
                LevelChunk chunk = minecraft.level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof BannerBlockEntity bannerBlockEntity) {
                        addUnique(stacks, bannerBlockEntity.getItem());
                    }
                }
            }
        }
    }

    private static void addBannerSwapVariant(List<ItemStack> stacks, ItemStack currentBanner) {
        DyeColor baseColor = getBannerBaseColor(currentBanner);
        ItemStack swapped;
        if (currentBanner.is(Items.SHIELD)) {
            swapped = new ItemStack(BannerPatternCatalog.itemByDyeId(baseColor.getId()));
            copyBannerBlockEntity(currentBanner, swapped);
            removeBannerBaseColor(swapped);
        } else {
            swapped = new ItemStack(Items.SHIELD);
            copyBannerBlockEntity(currentBanner, swapped);
            ItemStackNbt.getOrCreateElement(swapped, "BlockEntityTag").putInt("Base", baseColor.getId());
        }

        addUnique(stacks, swapped);
    }

    private static void addBannerPatternVariants(List<ItemStack> stacks, ItemStack currentBanner) {
        DyeColor baseColor = getBannerBaseColor(currentBanner);
        for (String patternHash : BannerPatternCatalog.patternHashes()) {
            for (DyeColor color : DyeColor.values()) {
                ItemStack variant = currentBanner.copy();
                variant.setCount(1);
                CompoundTag blockEntity = ItemStackNbt.getOrCreateElement(variant, "BlockEntityTag");
                if (variant.is(Items.SHIELD) && !blockEntity.contains("Base", Tag.TAG_INT)) {
                    blockEntity.putInt("Base", baseColor.getId());
                }

                ListTag patterns = blockEntity.contains("Patterns", Tag.TAG_LIST)
                        ? blockEntity.getList("Patterns", Tag.TAG_COMPOUND).copy()
                        : new ListTag();
                CompoundTag pattern = new CompoundTag();
                pattern.putString("Pattern", patternHash);
                pattern.putInt("Color", color.getId());
                patterns.add(pattern);
                blockEntity.put("Patterns", patterns);
                addUnique(stacks, variant);
            }
        }
    }

    private static boolean isBannerEditable(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (stack.getItem() instanceof BannerItem || stack.is(Items.SHIELD));
    }

    private static DyeColor getBannerBaseColor(ItemStack stack) {
        if (stack.getItem() instanceof BannerItem bannerItem) {
            return bannerItem.getColor();
        }

        CompoundTag blockEntity = ItemStackNbt.getElement(stack, "BlockEntityTag");
        if (blockEntity != null && blockEntity.contains("Base", Tag.TAG_INT)) {
            return DyeColor.byId(blockEntity.getInt("Base"));
        }

        return DyeColor.WHITE;
    }

    private static void copyBannerBlockEntity(ItemStack source, ItemStack target) {
        CompoundTag blockEntity = ItemStackNbt.getElement(source, "BlockEntityTag");
        if (blockEntity != null) {
            ItemStackNbt.getOrCreate(target).put("BlockEntityTag", blockEntity.copy());
        }
    }

    private static void removeBannerBaseColor(ItemStack stack) {
        CompoundTag tag = ItemStackNbt.get(stack);
        if (tag == null) {
            return;
        }

        CompoundTag blockEntity = tag.getCompound("BlockEntityTag");
        blockEntity.remove("Base");
        if (blockEntity.isEmpty()) {
            tag.remove("BlockEntityTag");
        } else {
            tag.put("BlockEntityTag", blockEntity);
        }
        if (tag.isEmpty()) {
            ItemStackNbt.set(stack, null);
        }
    }

    private static ItemStack createPlayerHead(String owner) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        ItemStackNbt.getOrCreate(stack).putString("SkullOwner", owner);
        CompoundTag display = ItemStackNbt.getOrCreateElement(stack, "display");
        display.putString("Name", ComponentCompat.toJson(net.minecraft.network.chat.Component.literal(owner + "'s Head")));
        return stack;
    }

    private static ItemStack createNote(String noteName, String... lore) {
        ItemStack stack = new ItemStack(Items.PAPER);
        ItemStackCompat.setHoverName(stack, Component.literal(noteName));
        if (lore != null && lore.length > 0) {
            ListTag loreTag = new ListTag();
            for (String line : lore) {
                loreTag.add(StringTag.valueOf(ComponentCompat.toJson(Component.literal(line))));
            }
            ItemStackNbt.getOrCreateElement(stack, "display").put("Lore", loreTag);
        }
        return stack;
    }

    private static boolean containsStack(List<ItemStack> stacks, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        for (ItemStack existingStack : stacks) {
            if (ItemStackCompat.isSameItemSameTags(existingStack, stack)) {
                return true;
            }
        }
        return false;
    }

    private static void addUnique(List<ItemStack> stacks, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        for (ItemStack existingStack : stacks) {
            if (ItemStackCompat.isSameItemSameTags(existingStack, copy)) {
                return;
            }
        }

        stacks.add(copy);
    }
}
