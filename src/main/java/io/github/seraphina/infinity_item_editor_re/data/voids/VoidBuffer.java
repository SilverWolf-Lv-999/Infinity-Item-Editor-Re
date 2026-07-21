package io.github.seraphina.infinity_item_editor_re.data.voids;

import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class VoidBuffer {
    private static final int MAX_SIZE = 300;

    private final BlockingDeque<ClientboundSetEquipmentPacket> queue = new LinkedBlockingDeque<>(MAX_SIZE);

    public ClientboundSetEquipmentPacket get() {
        try {
            return queue.take();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void put(ClientboundSetEquipmentPacket packet) {
        queue.offer(packet);
    }
}
