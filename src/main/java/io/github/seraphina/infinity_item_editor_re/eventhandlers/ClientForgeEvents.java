package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidController;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import io.github.seraphina.infinity_item_editor_re.mixin.CreativeModeInventoryScreenAccessor;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import io.github.seraphina.infinity_item_editor_re.util.PlayerInventorySlots;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {
    private static final String VOID_HANDLER = ModSource.MODID + "_void_handler";

    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        while (ClientKeyMappings.SAVE_REALM.consumeClick()) {
            RealmController realmController = ModSource.getOrCreateRealmController(minecraft.gameDirectory);
            if (realmController != null) {
                ItemStack heldStack = minecraft.player.getMainHandItem();
                realmController.addItemStack(minecraft.player, heldStack.copy());
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

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (handleContainerKeyShortcut(event.getScreen(), event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (handleContainerMouseShortcut(event.getScreen(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        if (!Config.getIsVoidEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        event.getMessage().visit((style, text) -> {
            addHoverItemToVoid(minecraft, style);
            return Optional.empty();
        }, Style.EMPTY);
    }

    @SubscribeEvent
    public static void onServerConnection(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!Config.getIsVoidEnabled() || event.getConnection().channel().pipeline().get(VOID_HANDLER) != null) {
            return;
        }

        event.getConnection().channel().pipeline().addBefore("packet_handler", VOID_HANDLER, new ChannelDuplexHandler() {
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

        Slot slot = containerScreen.getSlotUnderMouse();
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

        Slot slot = containerScreen.getSlotUnderMouse();
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
            if (isRealmCreativeTabSlot(minecraft, screen, slot)) {
                realmController.removeItemStack(minecraft.player, stack);
            } else {
                realmController.addItemStack(minecraft.player, stack.copy());
            }
        }

        if (Config.getIsVoidEnabled()) {
            new VoidController(stack).addItemStack(minecraft.player, stack.copy(), minecraft.player.getUUID().toString().replace("-", ""));
        }
        return true;
    }

    private static boolean isRealmCreativeTabSlot(Minecraft minecraft, Screen screen, Slot slot) {
        if (!(screen instanceof CreativeModeInventoryScreen) || PlayerInventorySlots.isPlayerInventorySlot(minecraft.player, slot)
                || !CreativeTabRegistry.REALM.isPresent()) {
            return false;
        }

        CreativeModeTab selectedTab = CreativeModeInventoryScreenAccessor.infinityItemEditorRe$getSelectedTab();
        return selectedTab == CreativeTabRegistry.REALM.get();
    }

    private static void addHoverItemToVoid(Minecraft minecraft, Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) {
            return;
        }

        HoverEvent.ItemStackInfo itemStackInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
        if (itemStackInfo == null) {
            return;
        }

        ItemStack stack = itemStackInfo.getItemStack();
        if (!stack.isEmpty()) {
            new VoidController(stack).addItemStack(minecraft.player, stack.copy(), "chat");
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

        ItemStack stack = blockState.getCloneItemStack(blockHitResult, minecraft.level, blockPos, minecraft.player);
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
                realmController.addItemStack(minecraft.player, stack.copy());
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
        CompoundTag blockEntityTag = blockEntity.saveWithFullMetadata();
        BlockItem.setBlockEntityData(stack, blockEntity.getType(), blockEntityTag);
        if (stack.getItem() instanceof PlayerHeadItem && blockEntityTag.contains("SkullOwner")) {
            CompoundTag skullOwner = blockEntityTag.getCompound("SkullOwner");
            CompoundTag stackTag = stack.getOrCreateTag();
            stackTag.put("SkullOwner", skullOwner);
            CompoundTag stackBlockEntityTag = stackTag.getCompound("BlockEntityTag");
            stackBlockEntityTag.remove("SkullOwner");
            stackBlockEntityTag.remove("x");
            stackBlockEntityTag.remove("y");
            stackBlockEntityTag.remove("z");
            return;
        }

        CompoundTag displayTag = new CompoundTag();
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("\"(+NBT)\""));
        displayTag.put("Lore", lore);
        stack.addTagElement("display", displayTag);
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
