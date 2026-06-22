package io.github.seraphina.infinity_item_editor_re.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.DyeColor;

class LorePixel {
    DyeColor color;
    LoreSymbol symbol;

    LorePixel() {
        this(DyeColor.WHITE, LoreSymbol.FULL_BLOCK);
    }

    LorePixel(DyeColor color, LoreSymbol symbol) {
        this.color = color;
        this.symbol = symbol;
    }

    LorePixel copy() {
        return new LorePixel(this.color, this.symbol);
    }

    String format() {
        ChatFormatting formatting = ChatFormatting.getById(15 - this.color.getId());
        return (formatting == null ? ChatFormatting.WHITE : formatting).toString();
    }

    @Override
    public String toString() {
        if (this.symbol.whitespace()) {
            return this.symbol.symbol();
        }
        return format() + this.symbol.symbol();
    }
}

enum LoreSymbol {
    FULL_BLOCK("fullblock", "\u2588", false),
    MEDIUM_SHADE("mediumshade", "\u2592", false),
    DARK_SHADE("darkshade", "\u2593", false),
    FULL_SPACE("fullspace", ChatFormatting.BOLD + " " + ChatFormatting.RESET + " ", true);

    private final String nameKey;
    private final String symbol;
    private final boolean whitespace;

    LoreSymbol(String nameKey, String symbol, boolean whitespace) {
        this.nameKey = nameKey;
        this.symbol = symbol;
        this.whitespace = whitespace;
    }

    String nameKey() {
        return this.nameKey;
    }

    String symbol() {
        return this.symbol;
    }

    boolean whitespace() {
        return this.whitespace;
    }
}
