package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.Config;
import io.github.seraphina.infinity_item_editor_re.ModSource;
import io.github.seraphina.infinity_item_editor_re.client.screen.ItemEditorScreen;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.util.GiveHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModSource.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientForgeEvents {
    private static final String VOID_HANDLER = ModSource.MODID + "_void_handler";
    private static final int OFFHAND_CONTAINER_SLOT = 45;
    private static final int HEAD_CONTAINER_SLOT = 5;
    private static final int CHEST_CONTAINER_SLOT = 6;
    private static final int LEGS_CONTAINER_SLOT = 7;
    private static final int FEET_CONTAINER_SLOT = 8;

    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
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
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.OFFHAND, OFFHAND_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.HEAD, HEAD_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.CHEST, CHEST_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.LEGS, LEGS_CONTAINER_SLOT);
        copyEquipmentSlot(minecraft, livingEntity, EquipmentSlot.FEET, FEET_CONTAINER_SLOT);

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
