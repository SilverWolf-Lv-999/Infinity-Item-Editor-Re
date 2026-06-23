package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PotterySherdCatalog {
    static final List<PotterySherdEntry> SHERDS = List.of(
            new PotterySherdEntry("brick", Items.BRICK),
            new PotterySherdEntry("angler", Items.ANGLER_POTTERY_SHERD),
            new PotterySherdEntry("archer", Items.ARCHER_POTTERY_SHERD),
            new PotterySherdEntry("arms_up", Items.ARMS_UP_POTTERY_SHERD),
            new PotterySherdEntry("blade", Items.BLADE_POTTERY_SHERD),
            new PotterySherdEntry("brewer", Items.BREWER_POTTERY_SHERD),
            new PotterySherdEntry("burn", Items.BURN_POTTERY_SHERD),
            new PotterySherdEntry("danger", Items.DANGER_POTTERY_SHERD),
            new PotterySherdEntry("explorer", Items.EXPLORER_POTTERY_SHERD),
            new PotterySherdEntry("flow", Items.FLOW_POTTERY_SHERD),
            new PotterySherdEntry("friend", Items.FRIEND_POTTERY_SHERD),
            new PotterySherdEntry("guster", Items.GUSTER_POTTERY_SHERD),
            new PotterySherdEntry("heart", Items.HEART_POTTERY_SHERD),
            new PotterySherdEntry("heartbreak", Items.HEARTBREAK_POTTERY_SHERD),
            new PotterySherdEntry("howl", Items.HOWL_POTTERY_SHERD),
            new PotterySherdEntry("miner", Items.MINER_POTTERY_SHERD),
            new PotterySherdEntry("mourner", Items.MOURNER_POTTERY_SHERD),
            new PotterySherdEntry("plenty", Items.PLENTY_POTTERY_SHERD),
            new PotterySherdEntry("prize", Items.PRIZE_POTTERY_SHERD),
            new PotterySherdEntry("scrape", Items.SCRAPE_POTTERY_SHERD),
            new PotterySherdEntry("sheaf", Items.SHEAF_POTTERY_SHERD),
            new PotterySherdEntry("shelter", Items.SHELTER_POTTERY_SHERD),
            new PotterySherdEntry("skull", Items.SKULL_POTTERY_SHERD),
            new PotterySherdEntry("snort", Items.SNORT_POTTERY_SHERD)
    );

    private PotterySherdCatalog() {
    }

    static PotterySherdEntry getByItem(Item item) {
        for (PotterySherdEntry entry : SHERDS) {
            if (entry.item() == item) {
                return entry;
            }
        }
        return SHERDS.getFirst();
    }
}
