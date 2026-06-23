package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.ClientCreativeTabData;
import io.github.seraphina.infinity_item_editor_re.client.CreativeTabRefresher;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.client.AbstractContainerScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.mixin.client.ConnectionAccessor;
import io.github.seraphina.infinity_item_editor_re.mixin.client.CreativeModeInventoryScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import io.github.seraphina.infinity_item_editor_re.util.PlayerInventorySlots;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> onChatReceived(message));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> onChatReceived(message));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onServerConnection(handler.getConnection()));
    }

    private static void onClientTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
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
                minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".editor_no_item"), true);
            } else {
                minecraft.setScreen(new ItemEditorScreen(heldStack.copy()));
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

    private static void onChatReceived(Component message) {
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

    private static void onServerConnection(Connection connection) {
        if (!Config.getIsVoidEnabled()) {
            return;
        }

        Channel channel = ((ConnectionAccessor) connection).infinityItemEditorRe$getChannel();
        if (channel == null || channel.pipeline().get(VOID_HANDLER) != null) {
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

        if (Screen.isCopy(keyCode)) {
            return copyHoveredStack(minecraft, slot);
        }
        if (Screen.isPaste(keyCode)) {
            return pasteHoveredStack(minecraft, slot);
        }
        if (ClientKeyMappings.OPEN_EDITOR.matches(keyCode, scanCode)) {
            return openHoveredSlotEditor(minecraft, containerScreen, slot);
        }
        if (ClientKeyMappings.SAVE_REALM.matches(keyCode, scanCode)) {
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

        if (ClientKeyMappings.OPEN_EDITOR.matchesMouse(button)) {
            return openHoveredSlotEditor(minecraft, containerScreen, slot);
        }
        if (ClientKeyMappings.SAVE_REALM.matchesMouse(button)) {
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

        minecraft.setScreen(new ItemEditorScreen(slot.getItem().copy(), containerSlot));
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
                || CreativeTabRegistry.REALM == null) {
            return false;
        }

        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab();
        return selectedTab == CreativeTabRegistry.REALM;
    }

    private static void handleHoverItem(Minecraft minecraft, Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) {
            return;
        }

        HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
        if (itemStackInfo == null) {
            return;
        }

        ItemStack stack = itemStackInfo.getItemStack();
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
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"), true);
            return true;
        }

        ItemStack pastedStack = GiveHelper.getItemStackFromString(
                minecraft.keyboardHandler.getClipboard(),
                minecraft.level.registryAccess().lookupOrThrow(Registries.ITEM)
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

        minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_no_target"), true);
    }

    private static void copyTargetEquipment(Minecraft minecraft, LivingEntity livingEntity) {
        if (!minecraft.player.getAbilities().instabuild) {
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"), true);
            return;
        }

        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.MAINHAND, 36 + minecraft.player.getInventory().selected);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.OFFHAND, PlayerInventorySlots.OFFHAND_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.HEAD, PlayerInventorySlots.HEAD_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.CHEST, PlayerInventorySlots.CHEST_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.LEGS, PlayerInventorySlots.LEGS_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.FEET, PlayerInventorySlots.FEET_CONTAINER_SLOT);

        minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copying", livingEntity.getDisplayName()), true);
    }

    private static void copyTargetBlock(Minecraft minecraft, BlockHitResult blockHitResult) {
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = minecraft.level.getBlockState(blockPos);
        if (blockState.isAir()) {
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_no_target"), true);
            return;
        }

        ItemStack stack = blockState.getBlock().getCloneItemStack(minecraft.level, blockPos, blockState);
        if (stack.isEmpty()) {
            minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_empty_block"), true);
            return;
        }

        boolean controlDown = Screen.hasControlDown();
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
                minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copy_requires_creative"), true);
            }
            return;
        }

        minecraft.player.getInventory().setPickedItem(stack);
        minecraft.gameMode.handleCreativeModeItemAdd(minecraft.player.getInventory().getSelected(), 36 + minecraft.player.getInventory().selected);
        minecraft.player.displayClientMessage(Component.translatable("message." + ModSource.MODID + ".copying", stack.getHoverName()), true);
    }

    private static void addCustomNbtData(ItemStack stack, BlockEntity blockEntity) {
        blockEntity.saveToItem(stack);
        if (stack.getItem() instanceof PlayerHeadItem) {
            CompoundTag stackTag = stack.getTag();
            if (stackTag != null && stackTag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
                CompoundTag stackBlockEntityTag = stackTag.getCompound(BLOCK_ENTITY_TAG);
                if (stackBlockEntityTag.contains(SKULL_OWNER_TAG, Tag.TAG_COMPOUND)) {
                    stackTag.put(SKULL_OWNER_TAG, stackBlockEntityTag.getCompound(SKULL_OWNER_TAG));
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
        return blockEntityTag.size() == 1 && blockEntityTag.contains(BLOCK_ENTITY_ID_TAG, Tag.TAG_STRING);
    }

    private static boolean hasCopiedBlockEntityData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND);
    }

    private static void addCopiedNbtLore(ItemStack stack) {
        CompoundTag displayTag = stack.getOrCreateTagElement(DISPLAY_TAG);
        ListTag lore = displayTag.contains(LORE_TAG, Tag.TAG_LIST)
                ? displayTag.getList(LORE_TAG, Tag.TAG_STRING).copy()
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

        switch (slot) {
            case MAINHAND -> minecraft.player.getInventory().items.set(minecraft.player.getInventory().selected, stack);
            case OFFHAND -> minecraft.player.getInventory().offhand.set(0, stack);
            case HEAD, CHEST, LEGS, FEET -> minecraft.player.getInventory().armor.set(slot.getIndex(), stack);
        }
    }
}
