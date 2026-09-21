package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.util.MinecraftCompat;

import io.github.seraphina.infinity_item_editor_re.util.NbtCompat;

import io.github.seraphina.infinity_item_editor_re.util.ItemStackNbt;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
import io.github.seraphina.infinity_item_editor_re.client.screen.CompatScreen;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.AbstractContainerScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.mixin.CreativeModeInventoryScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import io.github.seraphina.infinity_item_editor_re.util.PlayerInventorySlots;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import java.util.Optional;

public final class ClientEvents {
    private static final String VOID_HANDLER = ModSource.MODID + "_void_handler";
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String BLOCK_ENTITY_ID_TAG = "id";
    private static final String DISPLAY_TAG = "display";
    private static final String LORE_TAG = "Lore";
    private static final String SKULL_OWNER_TAG = "SkullOwner";
    private static final String COPIED_NBT_LORE = "\"(+NBT)\"";

    private ClientEvents() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || MinecraftCompat.screen(minecraft) != null) {
            return;
        }

        while (ClientKeyMappings.SAVE_REALM.consumeClick()) {
            RealmController realmController = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
            if (realmController != null) {
                ItemStack heldStack = minecraft.player.getMainHandItem();
                if (realmController.addItemStack(minecraft.player, heldStack.copy())) {
                    CreativeTabRefresher.refreshRealm(minecraft);
                }
            }
        }

        while (ClientKeyMappings.OPEN_EDITOR.consumeClick()) {
            ItemStack heldStack = minecraft.player.getMainHandItem();
            if (heldStack.isEmpty()) {
                minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".editor_no_item"));
            } else {
                MinecraftCompat.setScreen(minecraft, new ItemEditorScreen(heldStack.copy()));
            }
        }

        while (ClientKeyMappings.COPY_TARGET.consumeClick()) {
            copyTarget(minecraft);
        }
    }

    public static boolean handleScreenKeyPressed(Screen screen, int keyCode, int scanCode) {
        return handleContainerKeyShortcut(screen, keyCode, scanCode);
    }

    public static boolean handleScreenMousePressed(Screen screen, int button) {
        return handleContainerMouseShortcut(screen, button);
    }

    public static void onChatReceived(Component message) {
        if (!Config.getIsVoidEnabled() && !Config.getIsThiefTabEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        message.visit((style, text) -> {
            handleHoverItem(minecraft, style);
            return Optional.empty();
        }, Style.EMPTY);
    }

    public static void onServerConnection(Channel channel) {
        if (!Config.getIsVoidEnabled() || channel.pipeline().get(VOID_HANDLER) != null) {
            return;
        }

        channel.pipeline().addBefore("packet_handler", VOID_HANDLER, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                if (message instanceof ClientboundSetEquipmentPacket packet) {
                    ModSource.voidBuffer.put(packet);
                }
                super.channelRead(context, message);
            }
        });
    }

    private static boolean handleContainerKeyShortcut(Screen screen, int keyCode, int scanCode) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return false;
        }

        Slot slot = ((AbstractContainerScreenAccessor) containerScreen).infinityItemEditorRe$getHoveredSlot();
        if (slot == null) {
            return false;
        }

        if (CompatScreen.isCopy(keyCode)) {
            return copyHoveredStack(minecraft, slot);
        }
        if (CompatScreen.isPaste(keyCode)) {
            return pasteHoveredStack(minecraft, slot);
        }
        if (ClientKeyMappings.OPEN_EDITOR.matches(new KeyEvent(keyCode, scanCode, 0))) {
            return openHoveredSlotEditor(minecraft, containerScreen, slot);
        }
        if (ClientKeyMappings.SAVE_REALM.matches(new KeyEvent(keyCode, scanCode, 0))) {
            return saveHoveredStack(minecraft, screen, slot);
        }
        return false;
    }

    private static boolean handleContainerMouseShortcut(Screen screen, int button) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return false;
        }

        Slot slot = ((AbstractContainerScreenAccessor) containerScreen).infinityItemEditorRe$getHoveredSlot();
        if (slot == null) {
            return false;
        }

        MouseButtonEvent mouseEvent = new MouseButtonEvent(0.0, 0.0, new MouseButtonInfo(button, 0));
        if (ClientKeyMappings.OPEN_EDITOR.matchesMouse(mouseEvent)) {
            return openHoveredSlotEditor(minecraft, containerScreen, slot);
        }
        if (ClientKeyMappings.SAVE_REALM.matchesMouse(mouseEvent)) {
            return saveHoveredStack(minecraft, screen, slot);
        }
        return false;
    }

    private static boolean openHoveredSlotEditor(Minecraft minecraft, AbstractContainerScreen<?> containerScreen, Slot slot) {
        if (!PlayerInventorySlots.isPlayerInventorySlot(minecraft.player, slot)
                || !containerScreen.getMenu().getCarried().isEmpty()) {
            return false;
        }

        int containerSlot = PlayerInventorySlots.toContainerSlot(slot);
        if (containerSlot < 0) {
            return false;
        }

        MinecraftCompat.setScreen(minecraft, new ItemEditorScreen(slot.getItem().copy(), containerSlot));
        return true;
    }

    private static boolean saveHoveredStack(Minecraft minecraft, Screen screen, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || minecraft.player == null) {
            return false;
        }

        RealmController realmController = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
        if (realmController != null) {
            boolean realmChanged;
            if (isRealmCreativeTabSlot(minecraft, screen, slot)) {
                realmChanged = realmController.removeItemStack(minecraft.player, stack);
            } else {
                realmChanged = realmController.addItemStack(minecraft.player, stack.copy());
            }
            if (realmChanged) {
                CreativeTabRefresher.refreshRealm(minecraft);
            }
        }

        if (Config.getIsVoidEnabled()) {
            new VoidController(stack).addItemStack(minecraft.player, stack.copy(), minecraft.player.getUUID().toString().replace("-", ""));
        }
        return true;
    }

    private static boolean isRealmCreativeTabSlot(Minecraft minecraft, Screen screen, Slot slot) {
        if (!(screen instanceof CreativeModeInventoryScreen) || PlayerInventorySlots.isPlayerInventorySlot(minecraft.player, slot)
                || !CreativeTabRegistry.REALM.isBound()) {
            return false;
        }

        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab();
        return selectedTab == CreativeTabRegistry.REALM.get();
    }

    private static void handleHoverItem(Minecraft minecraft, Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) {
            return;
        }

        if (!(hoverEvent instanceof HoverEvent.ShowItem showItem)) {
            return;
        }

        ItemStack stack = showItem.item().create();
        if (stack.isEmpty()) {
            return;
        }

        if (Config.getIsVoidEnabled()) {
            new VoidController(stack).addItemStack(minecraft.player, stack.copy(), "chat");
        }
        if (Config.getIsThiefTabEnabled() && ClientCreativeTabData.rememberChatLinkedItem(stack)) {
            CreativeTabRefresher.refreshThief(minecraft);
        }
    }

    private static boolean copyHoveredStack(Minecraft minecraft, Slot slot) {
        if (!slot.hasItem()) {
            return false;
        }

        minecraft.keyboardHandler.setClipboard(GiveHelper.getStringFromItemStack(slot.getItem()));
        return true;
    }

    private static boolean pasteHoveredStack(Minecraft minecraft, Slot slot) {
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null
                || !PlayerInventorySlots.isPlayerInventorySlot(minecraft.player, slot)) {
            return false;
        }

        int containerSlot = PlayerInventorySlots.toContainerSlot(slot);
        if (containerSlot < 0) {
            return false;
        }

        if (!minecraft.player.getAbilities().instabuild) {
            minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"));
            return true;
        }

        ItemStack pastedStack = GiveHelper.getItemStackFromString(
                minecraft.keyboardHandler.getClipboard(),
                minecraft.level.registryAccess()
        );
        if (pastedStack.isEmpty()) {
            return false;
        }

        PlayerInventorySlots.setStack(minecraft.player, containerSlot, pastedStack);
        minecraft.gameMode.handleCreativeModeItemAdd(pastedStack.copy(), containerSlot);
        return true;
    }

    private static void copyTarget(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.gameMode == null) {
            return;
        }

        Entity target = minecraft.crosshairPickEntity;
        if (target == null && minecraft.hitResult instanceof EntityHitResult entityHitResult) {
            target = entityHitResult.getEntity();
        }

        if (target instanceof LivingEntity livingEntity) {
            copyTargetEquipment(minecraft, livingEntity);
            return;
        }

        if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
            copyTargetBlock(minecraft, blockHitResult);
            return;
        }

        minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_no_target"));
    }

    private static void copyTargetEquipment(Minecraft minecraft, LivingEntity livingEntity) {
        if (!minecraft.player.getAbilities().instabuild) {
            minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"));
            return;
        }

        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.MAINHAND, 36 + minecraft.player.getInventory().getSelectedSlot());
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.OFFHAND, PlayerInventorySlots.OFFHAND_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.HEAD, PlayerInventorySlots.HEAD_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.CHEST, PlayerInventorySlots.CHEST_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.LEGS, PlayerInventorySlots.LEGS_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.FEET, PlayerInventorySlots.FEET_CONTAINER_SLOT);

        minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copying", livingEntity.getDisplayName()));
    }

    private static void copyTargetBlock(Minecraft minecraft, BlockHitResult blockHitResult) {
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = minecraft.level.getBlockState(blockPos);
        if (blockState.isAir()) {
            minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_no_target"));
            return;
        }

        boolean controlDown = CompatScreen.hasControlDown();
        ItemStack stack = blockState.getCloneItemStack(minecraft.level, blockPos, controlDown);
        if (stack.isEmpty()) {
            minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_empty_block"));
            return;
        }

        if (controlDown && blockState.hasBlockEntity()) {
            BlockEntity blockEntity = minecraft.level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                addCustomNbtData(stack, blockEntity);
            }
        }

        stack.setCount(1);
        minecraft.keyboardHandler.setClipboard(GiveHelper.getStringFromItemStack(stack));
        if (controlDown) {
            RealmController realmController = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
            if (realmController != null) {
                if (realmController.addItemStack(minecraft.player, stack.copy())) {
                    CreativeTabRefresher.refreshRealm(minecraft);
                }
            }
        }

        if (!minecraft.player.getAbilities().instabuild) {
            if (!controlDown) {
                minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"));
            }
            return;
        }

        setPickedItem(minecraft, stack);
        minecraft.gameMode.handleCreativeModeItemAdd(minecraft.player.getInventory().getSelectedItem(), 36 + minecraft.player.getInventory().getSelectedSlot());
        minecraft.player.sendOverlayMessage(Component.translatable("message." + ModSource.MODID + ".copying", stack.getHoverName()));
    }

    private static void addCustomNbtData(ItemStack stack, BlockEntity blockEntity) {
        TagValueOutput blockEntityOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, ItemStackNbt.provider());
        blockEntity.saveCustomOnly(blockEntityOutput);
        blockEntity.removeComponentsFromTag(blockEntityOutput);
        BlockItem.setBlockEntityData(stack, blockEntity.getType(), blockEntityOutput);
        CompoundTag blockEntityTag = blockEntityOutput.buildResult();
        stack.applyComponents(blockEntity.collectComponents());
        if (stack.getItem() instanceof PlayerHeadItem) {
            CompoundTag stackTag = ItemStackNbt.get(stack);
            if (stackTag != null && NbtCompat.contains(stackTag, BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
                CompoundTag stackBlockEntityTag = NbtCompat.getCompound(stackTag, BLOCK_ENTITY_TAG);
                if (NbtCompat.contains(stackBlockEntityTag, SKULL_OWNER_TAG, Tag.TAG_COMPOUND)) {
                    stackTag.put(SKULL_OWNER_TAG, NbtCompat.getCompound(stackBlockEntityTag, SKULL_OWNER_TAG));
                    stackBlockEntityTag.remove(SKULL_OWNER_TAG);
                    cleanupCopiedBlockEntityTag(stackTag, stackBlockEntityTag);
                    return;
                }
            }
        }

        if (hasCopiedBlockEntityData(stack)) {
            addCopiedNbtLore(stack);
        }
    }

    private static void setPickedItem(Minecraft minecraft, ItemStack stack) {
        Inventory inventory = minecraft.player.getInventory();
        int slot = inventory.findSlotMatchingItem(stack);
        if (slot != -1) {
            if (Inventory.isHotbarSlot(slot)) {
                inventory.setSelectedSlot(slot);
            } else {
                inventory.pickSlot(slot);
            }
        } else {
            inventory.addAndPickItem(stack);
        }
    }

    private static void cleanupCopiedBlockEntityTag(CompoundTag stackTag, CompoundTag blockEntityTag) {
        blockEntityTag.remove("x");
        blockEntityTag.remove("y");
        blockEntityTag.remove("z");
        if (blockEntityTag.isEmpty() || isOnlyBlockEntityId(blockEntityTag)) {
            stackTag.remove(BLOCK_ENTITY_TAG);
        } else {
            stackTag.put(BLOCK_ENTITY_TAG, blockEntityTag);
        }
    }

    private static boolean isOnlyBlockEntityId(CompoundTag blockEntityTag) {
        return blockEntityTag.size() == 1 && NbtCompat.contains(blockEntityTag, BLOCK_ENTITY_ID_TAG, Tag.TAG_STRING);
    }

    private static boolean hasCopiedBlockEntityData(ItemStack stack) {
        CompoundTag tag = ItemStackNbt.get(stack);
        return tag != null && NbtCompat.contains(tag, BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND);
    }

    private static void addCopiedNbtLore(ItemStack stack) {
        CompoundTag displayTag = ItemStackNbt.getOrCreateElement(stack, DISPLAY_TAG);
        ListTag lore = NbtCompat.contains(displayTag, LORE_TAG, Tag.TAG_LIST)
                ? NbtCompat.getList(displayTag, LORE_TAG, Tag.TAG_STRING).copy()
                : new ListTag();
        lore.add(StringTag.valueOf(COPIED_NBT_LORE));
        displayTag.put(LORE_TAG, lore);
    }

    private static void copyEquipmentSlot(Minecraft minecraft, LivingEntity source, EquipmentSlot slot, int containerSlot) {
        ItemStack stack = source.getItemBySlot(slot).copy();
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        minecraft.gameMode.handleCreativeModeItemAdd(stack, containerSlot);
        PlayerInventorySlots.setStack(minecraft.player, containerSlot, stack);
    }
}
