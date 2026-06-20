package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class BannerPatternCatalog {
    static final Item[] ITEMS_BY_DYE = {
            Items.WHITE_BANNER,
            Items.ORANGE_BANNER,
            Items.MAGENTA_BANNER,
            Items.LIGHT_BLUE_BANNER,
            Items.YELLOW_BANNER,
            Items.LIME_BANNER,
            Items.PINK_BANNER,
            Items.GRAY_BANNER,
            Items.LIGHT_GRAY_BANNER,
            Items.CYAN_BANNER,
            Items.PURPLE_BANNER,
            Items.BLUE_BANNER,
            Items.BROWN_BANNER,
            Items.GREEN_BANNER,
            Items.RED_BANNER,
            Items.BLACK_BANNER
    };

    static final List<BannerPatternEntry> PATTERNS = List.of(
            new BannerPatternEntry("square_bottom_left", "bl"),
            new BannerPatternEntry("square_bottom_right", "br"),
            new BannerPatternEntry("square_top_left", "tl"),
            new BannerPatternEntry("square_top_right", "tr"),
            new BannerPatternEntry("stripe_bottom", "bs"),
            new BannerPatternEntry("stripe_top", "ts"),
            new BannerPatternEntry("stripe_left", "ls"),
            new BannerPatternEntry("stripe_right", "rs"),
            new BannerPatternEntry("stripe_center", "cs"),
            new BannerPatternEntry("stripe_middle", "ms"),
            new BannerPatternEntry("stripe_downright", "drs"),
            new BannerPatternEntry("stripe_downleft", "dls"),
            new BannerPatternEntry("small_stripes", "ss"),
            new BannerPatternEntry("cross", "cr"),
            new BannerPatternEntry("straight_cross", "sc"),
            new BannerPatternEntry("triangle_bottom", "bt"),
            new BannerPatternEntry("triangle_top", "tt"),
            new BannerPatternEntry("triangles_bottom", "bts"),
            new BannerPatternEntry("triangles_top", "tts"),
            new BannerPatternEntry("diagonal_left", "ld"),
            new BannerPatternEntry("diagonal_up_right", "rd"),
            new BannerPatternEntry("diagonal_up_left", "lud"),
            new BannerPatternEntry("diagonal_right", "rud"),
            new BannerPatternEntry("circle", "mc"),
            new BannerPatternEntry("rhombus", "mr"),
            new BannerPatternEntry("half_vertical", "vh"),
            new BannerPatternEntry("half_horizontal", "hh"),
            new BannerPatternEntry("half_vertical_right", "vhr"),
            new BannerPatternEntry("half_horizontal_bottom", "hhb"),
            new BannerPatternEntry("border", "bo"),
            new BannerPatternEntry("curly_border", "cbo"),
            new BannerPatternEntry("gradient", "gra"),
            new BannerPatternEntry("gradient_up", "gru"),
            new BannerPatternEntry("bricks", "bri"),
            new BannerPatternEntry("globe", "glb"),
            new BannerPatternEntry("creeper", "cre"),
            new BannerPatternEntry("skull", "sku"),
            new BannerPatternEntry("flower", "flo"),
            new BannerPatternEntry("mojang", "moj"),
            new BannerPatternEntry("piglin", "pig")
    );

    private BannerPatternCatalog() {
    }

    public static Item itemByDyeId(int dyeId) {
        return ITEMS_BY_DYE[Math.floorMod(dyeId, ITEMS_BY_DYE.length)];
    }

    public static List<String> patternHashes() {
        return PATTERNS.stream()
                .map(BannerPatternEntry::hash)
                .toList();
    }
}
