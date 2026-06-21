package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class ModernUi {
    public static final int TEXT_PRIMARY = 0xFFF4F5EF;
    public static final int TEXT_SECONDARY = 0xFFD4D7CF;
    public static final int TEXT_MUTED = 0xFF939B93;
    public static final int ACCENT = 0xFFFFB347;
    public static final int ACCENT_SOFT = 0x66FFB347;
    public static final int ACCENT_HOVER = 0xFFFFD073;
    public static final int WARM = 0xFF62D1C7;
    public static final int WARM_SOFT = 0x4062D1C7;
    public static final int DANGER = 0xFFFF5F66;
    public static final int SUCCESS = 0xFF72D879;

    public static final int BACKDROP_TOP = 0xF20E1113;
    public static final int BACKDROP_BOTTOM = 0xF21D2020;
    public static final int BACKDROP_GLOW = 0x2EFFB347;
    public static final int SIDEBAR_TOP = 0xF2241517;
    public static final int SIDEBAR_BOTTOM = 0xF2141719;
    public static final int SURFACE = 0xE11D2428;
    public static final int SURFACE_SOFT = 0xC1262D30;
    public static final int SURFACE_STRONG = 0xF12D3435;
    public static final int SURFACE_HOVER = 0xF13B4546;
    public static final int SURFACE_PRESSED = 0xF014181A;
    public static final int SURFACE_DISABLED = 0x92282D2F;
    public static final int BORDER = 0x7C68706A;
    public static final int BORDER_SOFT = 0x38595F5A;
    public static final int BORDER_STRONG = 0xD7FFB347;
    public static final int FOCUS_RING = 0x66FFB347;
    public static final int SHADOW = 0x86000000;
    public static final int SHADOW_SOFT = 0x40000000;
    public static final int HIGHLIGHT = 0x24FFFFFF;
    public static final int CONTROL_RED = 0xFFC84942;
    public static final int CONTROL_YELLOW = 0xFFFFBF54;
    public static final int CONTROL_GREEN = 0xFF68C47B;

    private static final int TOOLBOX_RED_TOP = 0xE66D2528;
    private static final int TOOLBOX_RED_BOTTOM = 0xE641191B;
    private static final int TOOLBOX_RAIL = 0xF0343A3A;
    private static final int TOOLBOX_DARK_RAIL = 0xF0101416;
    private static final int TOOLBOX_SLOT = 0xF0121719;
    private static final int TOOLBOX_HANDLE = 0xF03C4342;

    private ModernUi() {
    }

    public static void fillToolboxBackdrop(GuiGraphics guiGraphics, int width, int height) {
        guiGraphics.fillGradient(0, 0, width, height, BACKDROP_TOP, BACKDROP_BOTTOM);
        for (int y = 0; y < height; y += 18) {
            guiGraphics.fill(0, y, width, y + 1, alpha(0xFFFFFF, 7));
        }
    }

    public static void fillToolboxSidebar(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.fillGradient(x1, y1, x2, y2, TOOLBOX_RED_TOP, TOOLBOX_RED_BOTTOM);
        guiGraphics.fill(x2 - 6, y1, x2 - 1, y2, SHADOW_SOFT);
        guiGraphics.fill(x2 - 1, y1 + 12, x2, y2 - 12, BORDER_STRONG);
        guiGraphics.fill(x1 + 3, y1 + 8, x2 - 8, y1 + 9, alpha(0xFFFFFF, 28));
        guiGraphics.fill(x1 + 3, y2 - 9, x2 - 8, y2 - 8, alpha(0x000000, 72));
        drawVentSlots(guiGraphics, x1 + 9, y2 - 66, x2 - 16, 5, 7);
    }

    public static void fillWorkSurface(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        fillPanel(guiGraphics, x1, y1, x2, y2, 6, SURFACE_SOFT, BORDER);
        fillRounded(guiGraphics, x1 + 10, y1 + 8, x2 - 10, y1 + 14, 3, TOOLBOX_RAIL);
        guiGraphics.fill(x1 + 18, y1 + 10, x2 - 18, y1 + 11, alpha(0xFFFFFF, 18));
        drawRivets(guiGraphics, x1 + 16, y1 + 8, x2 - 16, y1 + 8);
    }

    public static void fillToolDrawer(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, boolean selected) {
        int fill = selected ? lerpColor(SURFACE_STRONG, TOOLBOX_RED_TOP, 0.38F) : SURFACE;
        int border = selected ? BORDER_STRONG : BORDER;
        fillPanel(guiGraphics, x1, y1, x2, y2, 5, fill, border);
        fillRounded(guiGraphics, x1 + 7, y1 + 6, x2 - 7, y1 + 11, 3, TOOLBOX_HANDLE);
        guiGraphics.fill(x1 + 12, y1 + 8, x2 - 12, y1 + 9, alpha(0xFFFFFF, selected ? 35 : 18));
        if (selected) {
            guiGraphics.fill(x1 + 3, y1 + 15, x1 + 6, y2 - 5, ACCENT_HOVER);
        }
    }

    public static void fillToolButton(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2,
                                      boolean highlighted, boolean active, float hoverAmount) {
        int fill = active ? lerpColor(SURFACE, SURFACE_HOVER, hoverAmount) : SURFACE_DISABLED;
        int border = highlighted && active ? BORDER_STRONG : BORDER;
        int radius = Math.min(5, Math.max(3, (y2 - y1) / 3));
        if (highlighted && active) {
            fillRounded(guiGraphics, x1 - 1, y1 - 1, x2 + 1, y2 + 1, radius + 1,
                    alpha(0xFFB347, Math.round(38.0F * hoverAmount)));
        }

        fillPanel(guiGraphics, x1, y1, x2, y2, radius, fill, border);
        guiGraphics.fill(x1 + 3, y1 + 4, x1 + 5, y2 - 4, active ? lerpColor(ACCENT, WARM, hoverAmount) : alpha(0xFFFFFF, 28));
        if (x2 - x1 >= 28) {
            guiGraphics.fill(x2 - 8, y1 + 5, x2 - 6, y1 + 7, alpha(0xFFFFFF, highlighted ? 65 : 30));
            guiGraphics.fill(x2 - 8, y2 - 7, x2 - 6, y2 - 5, alpha(0x000000, 72));
        }
    }

    public static void fillToolInput(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2,
                                     boolean focused, boolean active) {
        if (focused && active) {
            fillRounded(guiGraphics, x1 - 2, y1 - 2, x2 + 2, y2 + 2, 6, FOCUS_RING);
        }
        int border = !active ? 0x554D514D : focused ? BORDER_STRONG : BORDER;
        int fill = !active ? SURFACE_DISABLED : TOOLBOX_SLOT;
        fillRounded(guiGraphics, x1, y1, x2, y2, 5, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 4, fill);
        guiGraphics.fill(x1 + 5, y1 + 2, x2 - 5, y1 + 3, alpha(0xFFFFFF, active ? 24 : 9));
        guiGraphics.fill(x1 + 4, y2 - 3, x2 - 4, y2 - 2, alpha(0x000000, 72));
        if (focused && active) {
            guiGraphics.fill(x1 + 4, y2 - 2, x2 - 4, y2 - 1, ACCENT_HOVER);
        }
    }

    public static void fillToolPaletteCell(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2,
                                           boolean highlighted, boolean active) {
        int fill = active ? (highlighted ? SURFACE_HOVER : SURFACE) : SURFACE_DISABLED;
        fillRounded(guiGraphics, x1, y1, x2, y2, 3, highlighted && active ? BORDER_STRONG : BORDER);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 2, fill);
        if (highlighted && active) {
            guiGraphics.fill(x1 + 2, y2 - 3, x2 - 2, y2 - 2, ACCENT_HOVER);
        }
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
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        fillRounded(guiGraphics, x1 + 2, y1 + 3, x2 + 2, y2 + 3, radius, SHADOW);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 + 1, y2 + 2, radius, SHADOW_SOFT);
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        guiGraphics.fillGradient(x1 + 1, y1 + 1, x2 - 1, Math.min(y2 - 1, y1 + 16),
                alpha(0xFFFFFF, 22), alpha(0xFFFFFF, 3));
        if (x2 - x1 > radius * 2) {
            guiGraphics.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, alpha(0xFFFFFF, 40));
            guiGraphics.fill(x1 + radius, y2 - 2, x2 - radius, y2 - 1, alpha(0x000000, 82));
        }
    }

    public static void fillGlassPanel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius) {
        fillWorkSurface(guiGraphics, x1, y1, x2, y2);
        guiGraphics.fillGradient(x1 + 2, y1 + 15, x2 - 2, y2 - 2, alpha(0x62D1C7, 12), alpha(0x000000, 0));
    }

    public static void fillHeaderPanel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius) {
        fillToolDrawer(guiGraphics, x1, y1, x2, y2, true);
        guiGraphics.fillGradient(x1 + 1, y1 + 1, x2 - 1, y1 + Math.min(24, Math.max(2, y2 - y1 - 1)),
                alpha(0xFFFFFF, 26), alpha(0xFFB347, 16));
    }

    public static void fillContentGlow(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.fillGradient(x1, y1, x2, y2, BACKDROP_GLOW, alpha(0x62D1C7, 0));
        for (int x = x1 + 12; x < x2; x += 28) {
            guiGraphics.fill(x, y1 + 10, Math.min(x + 12, x2), y1 + 12, alpha(0xFFB347, 20));
        }
    }

    public static void fillInset(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, boolean focused, boolean active) {
        fillToolInput(guiGraphics, x1, y1, x2, y2, focused, active);
    }

    public static void fillPill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int fill, int border) {
        fillPanel(guiGraphics, x1, y1, x2, y2, Math.min(radius, 5), fill, border);
    }

    public static void fillSlot(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        fillRounded(guiGraphics, x - 2, y - 2, x + 18, y + 18, 4, selected ? BORDER_STRONG : BORDER);
        fillRounded(guiGraphics, x - 1, y - 1, x + 17, y + 17, 3, TOOLBOX_SLOT);
        guiGraphics.fill(x + 1, y, x + 15, y + 1, alpha(0xFFFFFF, 28));
        guiGraphics.fill(x + 1, y + 15, x + 15, y + 16, alpha(0x000000, 82));
        if (selected) {
            guiGraphics.fill(x - 1, y + 17, x + 17, y + 18, ACCENT_HOVER);
            guiGraphics.fill(x - 2, y - 2, x + 1, y + 1, ACCENT_HOVER);
            guiGraphics.fill(x + 15, y - 2, x + 18, y + 1, ACCENT_HOVER);
        }
    }

    public static void fillSelection(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, boolean emphasized) {
        int fill = emphasized ? alpha(0xFFB347, 72) : alpha(0xFFFFFF, 18);
        fillRounded(guiGraphics, x1, y1, x2, y2, Math.min(radius, 4), fill);
        guiGraphics.fill(x1 + 2, y1 + 2, x1 + 5, y2 - 2, emphasized ? ACCENT_HOVER : WARM);
        guiGraphics.fill(x2 - 4, y1 + 3, x2 - 2, y2 - 3, alpha(0x000000, 55));
    }

    public static void drawWindowControls(GuiGraphics guiGraphics, int x, int y) {
        fillRounded(guiGraphics, x, y, x + 6, y + 6, 3, CONTROL_RED);
        fillRounded(guiGraphics, x + 10, y, x + 16, y + 6, 3, CONTROL_YELLOW);
        fillRounded(guiGraphics, x + 20, y, x + 26, y + 6, 3, CONTROL_GREEN);
        guiGraphics.fill(x + 2, y + 1, x + 4, y + 2, alpha(0xFFFFFF, 85));
        guiGraphics.fill(x + 12, y + 1, x + 14, y + 2, alpha(0xFFFFFF, 85));
        guiGraphics.fill(x + 22, y + 1, x + 24, y + 2, alpha(0xFFFFFF, 85));
    }

    public static void fillItemWell(GuiGraphics guiGraphics, int centerX, int centerY, int size) {
        int half = size / 2;
        fillRounded(guiGraphics, centerX - half - 2, centerY - half + 3, centerX + half + 2, centerY + half + 6, 5, SHADOW_SOFT);
        fillRounded(guiGraphics, centerX - half, centerY - half, centerX + half, centerY + half, 5, BORDER);
        fillRounded(guiGraphics, centerX - half + 1, centerY - half + 1, centerX + half - 1, centerY + half - 1, 4, TOOLBOX_SLOT);
        guiGraphics.fill(centerX - half + 6, centerY - half + 2, centerX + half - 6, centerY - half + 3, alpha(0xFFFFFF, 35));
        guiGraphics.fill(centerX - half + 6, centerY + half - 3, centerX + half - 6, centerY + half - 2, alpha(0x000000, 80));
    }

    public static void fillSoftHalo(GuiGraphics guiGraphics, int centerX, int centerY, int width, int height, int color) {
        fillRounded(guiGraphics, centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, Math.min(6, height / 2), color);
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

    private static void drawRivets(GuiGraphics guiGraphics, int leftX, int y, int rightX, int rightY) {
        fillRounded(guiGraphics, leftX, y, leftX + 4, y + 4, 2, alpha(0xFFFFFF, 38));
        fillRounded(guiGraphics, leftX + 1, y + 1, leftX + 3, y + 3, 1, alpha(0x000000, 72));
        fillRounded(guiGraphics, rightX - 4, rightY, rightX, rightY + 4, 2, alpha(0xFFFFFF, 38));
        fillRounded(guiGraphics, rightX - 3, rightY + 1, rightX - 1, rightY + 3, 1, alpha(0x000000, 72));
    }

    private static void drawVentSlots(GuiGraphics guiGraphics, int x1, int y1, int x2, int rows, int gap) {
        if (x2 <= x1) {
            return;
        }
        for (int i = 0; i < rows; i++) {
            int y = y1 + i * gap;
            fillRounded(guiGraphics, x1, y, x2, y + 2, 1, alpha(0x000000, 75));
            guiGraphics.fill(x1 + 2, y, x2 - 2, y + 1, alpha(0xFFFFFF, 16));
        }
    }

    private static int cornerInset(int radius, int row) {
        float normalized = (row + 0.5F) / radius;
        float curve = (float) Math.sqrt(Math.max(0.0F, 1.0F - normalized * normalized));
        return Math.max(0, Math.round(radius - radius * curve));
    }
}
