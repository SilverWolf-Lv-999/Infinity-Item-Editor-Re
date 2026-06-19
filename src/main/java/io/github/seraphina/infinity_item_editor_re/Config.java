package io.github.seraphina.infinity_item_editor_re;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ModSource.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ITEM_GUI_SIDEBAR = BUILDER
            .comment("Whether the item editor GUI should use the sidebar layout once the GUI is ported.")
            .define("itemGuiSidebar", false);
    private static final ForgeConfigSpec.BooleanValue VOID_TAB = BUILDER
            .comment("Whether the Infinity Void creative tab is shown.")
            .define("voidTab", true);
    private static final ForgeConfigSpec.BooleanValue VOID_ADD_NOTIFICATION = BUILDER
            .comment("Whether newly discovered Infinity Void entries should notify the player.")
            .define("voidAddNotification", false);
    private static final ForgeConfigSpec.BooleanValue VOID_TAB_HIDE_HEADS = BUILDER
            .comment("Whether player heads should be hidden from the Infinity Void tab.")
            .define("voidTabHideHeads", false);
    private static final ForgeConfigSpec.BooleanValue UNAVAILABLE_TAB = BUILDER
            .comment("Whether the unavailable-items creative tab is shown.")
            .define("unavailableTab", true);
    private static final ForgeConfigSpec.BooleanValue BANNER_TAB = BUILDER
            .comment("Whether the banner helper creative tab is shown.")
            .define("bannerTab", true);
    private static final ForgeConfigSpec.BooleanValue HEAD_TAB = BUILDER
            .comment("Whether the head helper creative tab is shown.")
            .define("headTab", true);
    private static final ForgeConfigSpec.BooleanValue THIEF_TAB = BUILDER
            .comment("Whether the thief creative tab is shown.")
            .define("thiefTab", true);
    private static final ForgeConfigSpec.BooleanValue FIREWORK_TAB = BUILDER
            .comment("Whether the firework helper creative tab is shown.")
            .define("fireworkTab", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean itemGuiSidebar = false;
    public static boolean voidTab = true;
    public static boolean voidAddNotification = false;
    public static boolean voidTabHideHeads = false;
    public static boolean unavailableTab = true;
    public static boolean bannerTab = true;
    public static boolean headTab = true;
    public static boolean thiefTab = true;
    public static boolean fireworkTab = true;

    public static final int MAIN_COLOR = colorFromRgba(255, 150, 0, 200);
    public static final int ALT_COLOR = colorFromRgba(255, 50, 20, 75);
    public static final int CONTRAST_COLOR = colorFromRgba(255, 0, 100, 255);

    private Config() {
    }

    public static boolean getItemSidebar() {
        return itemGuiSidebar;
    }

    public static boolean getIsVoidEnabled() {
        return voidTab;
    }

    public static boolean getIsUnavailableTabEnabled() {
        return unavailableTab;
    }

    public static boolean getIsBannerTabEnabled() {
        return bannerTab;
    }

    public static boolean getIsHeadTabEnabled() {
        return headTab;
    }

    public static boolean getIsThiefTabEnabled() {
        return thiefTab;
    }

    public static boolean getIsFireworkTabEnabled() {
        return fireworkTab;
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        itemGuiSidebar = ITEM_GUI_SIDEBAR.get();
        voidTab = VOID_TAB.get();
        voidAddNotification = VOID_ADD_NOTIFICATION.get();
        voidTabHideHeads = VOID_TAB_HIDE_HEADS.get();
        unavailableTab = UNAVAILABLE_TAB.get();
        bannerTab = BANNER_TAB.get();
        headTab = HEAD_TAB.get();
        thiefTab = THIEF_TAB.get();
        fireworkTab = FIREWORK_TAB.get();
    }

    private static int colorFromRgba(int alpha, int red, int green, int blue) {
        return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }
}
