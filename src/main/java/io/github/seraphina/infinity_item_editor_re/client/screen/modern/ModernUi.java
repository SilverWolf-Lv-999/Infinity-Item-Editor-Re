package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class ModernUi {
    public static final int TEXT_PRIMARY = 0xFFEAF4F0;
    public static final int TEXT_SECONDARY = 0xFFC2D0CC;
    public static final int TEXT_MUTED = 0xFF91A29D;
    public static final int ACCENT = 0xFF38D6C5;
    public static final int ACCENT_SOFT = 0x6638D6C5;
    public static final int ACCENT_HOVER = 0xFF70F0DD;
    public static final int WARM = 0xFFFFB84D;
    public static final int WARM_SOFT = 0x55FFB84D;
    public static final int DANGER = 0xFFFF6078;
    public static final int SUCCESS = 0xFF65E68A;

    public static final int BACKDROP_TOP = 0xF20B1012;
    public static final int BACKDROP_BOTTOM = 0xF218171F;
    public static final int SIDEBAR_TOP = 0xF1121718;
    public static final int SIDEBAR_BOTTOM = 0xF01C1721;
    public static final int SURFACE = 0xDC1B2527;
    public static final int SURFACE_SOFT = 0xB6243032;
    public static final int SURFACE_STRONG = 0xF0232F31;
    public static final int SURFACE_HOVER = 0xF02B3B3C;
    public static final int SURFACE_DISABLED = 0x9A24282A;
    public static final int BORDER = 0x664B6965;
    public static final int BORDER_STRONG = 0xAA5FD9CA;
    public static final int SHADOW = 0x78000000;

    private ModernUi() {
    }

    public static void fillRounded(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }

        int maxRadius = Math.min((x2 - x1) / 2, (y2 - y1) / 2);
        radius = Mth.clamp(radius, 0, Math.max(0, maxRadius));
        if (radius <= 1) {
            guiGraphics.fill(x1, y1, x2, y2, color);
            return;
        }

        guiGraphics.fill(x1 + radius, y1, x2 - radius, y2, color);
        guiGraphics.fill(x1, y1 + radius, x2, y2 - radius, color);
        for (int i = 0; i < radius; i++) {
            int inset = cornerInset(radius, i);
            guiGraphics.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            guiGraphics.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    public static void fillPanel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int fill, int border) {
        fillRounded(guiGraphics, x1 + 2, y1 + 3, x2 + 2, y2 + 3, radius, SHADOW);
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        guiGraphics.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, alpha(0xFFFFFF, 31));
    }

    public static void fillInset(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, boolean focused, boolean active) {
        int border = !active ? 0x554B5553 : focused ? BORDER_STRONG : BORDER;
        int fill = !active ? SURFACE_DISABLED : 0xD6101719;
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        if (focused && active) {
            guiGraphics.fill(x1 + radius, y2 - 2, x2 - radius, y2 - 1, ACCENT);
        }
    }

    public static void fillPill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int fill, int border) {
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
    }

    public static void fillSlot(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        fillRounded(guiGraphics, x - 2, y - 2, x + 18, y + 18, 4, selected ? WARM : BORDER);
        fillRounded(guiGraphics, x - 1, y - 1, x + 17, y + 17, 3, 0xF00F1516);
        if (selected) {
            guiGraphics.fill(x, y + 15, x + 16, y + 16, WARM);
        }
    }

    public static int alpha(int rgb, int alpha) {
        return ((alpha & 255) << 24) | (rgb & 0xFFFFFF);
    }

    public static int lerpColor(int from, int to, float amount) {
        amount = Mth.clamp(amount, 0.0F, 1.0F);
        int a = Mth.lerpInt(amount, from >>> 24 & 255, to >>> 24 & 255);
        int r = Mth.lerpInt(amount, from >>> 16 & 255, to >>> 16 & 255);
        int g = Mth.lerpInt(amount, from >>> 8 & 255, to >>> 8 & 255);
        int b = Mth.lerpInt(amount, from & 255, to & 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int cornerInset(int radius, int row) {
        float normalized = (row + 0.5F) / radius;
        float curve = (float) Math.sqrt(Math.max(0.0F, 1.0F - normalized * normalized));
        return Math.max(0, Math.round(radius - radius * curve));
    }
}
