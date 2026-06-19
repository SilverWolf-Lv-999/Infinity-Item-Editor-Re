package io.github.seraphina.infinity_item_editor_re.data.voids;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VoidConsumer implements Runnable {
    private final VoidBuffer buffer;

    public VoidConsumer(VoidBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ClientboundSetEquipmentPacket packet = buffer.get();
            if (packet == null) {
                continue;
            }

            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> processPacket(minecraft, packet));
        }
    }

    private static void processPacket(Minecraft minecraft, ClientboundSetEquipmentPacket packet) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(packet.getEntity());
        String uuid = entity instanceof Player player ? player.getStringUUID().replace("-", "") : null;

        for (Pair<EquipmentSlot, ItemStack> slot : packet.getSlots()) {
            ItemStack stack = slot.getSecond();
            if (!stack.isEmpty()) {
                new VoidController(stack).addItemStack(minecraft.player, stack.copy(), uuid);
            }
        }
    }
}
