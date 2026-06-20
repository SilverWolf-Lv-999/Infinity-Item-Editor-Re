package io.github.seraphina.infinity_item_editor_re.client.screen.modern;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class ModernUi {
    public static final int TEXT_PRIMARY = 0xFFF5F7FA;
    public static final int TEXT_SECONDARY = 0xFFD3DAE3;
    public static final int TEXT_MUTED = 0xFF9AA8B7;
    public static final int ACCENT = 0xFF62D6FF;
    public static final int ACCENT_SOFT = 0x5A62D6FF;
    public static final int ACCENT_HOVER = 0xFFA9ECFF;
    public static final int WARM = 0xFFFFC56E;
    public static final int WARM_SOFT = 0x48FFC56E;
    public static final int DANGER = 0xFFFF6078;
    public static final int SUCCESS = 0xFF65E68A;

    public static final int BACKDROP_TOP = 0xF20B1118;
    public static final int BACKDROP_BOTTOM = 0xF21A1823;
    public static final int BACKDROP_GLOW = 0x3262D6FF;
    public static final int SIDEBAR_TOP = 0xEE131923;
    public static final int SIDEBAR_BOTTOM = 0xEE1D1925;
    public static final int SURFACE = 0xD81E2732;
    public static final int SURFACE_SOFT = 0xB9273340;
    public static final int SURFACE_STRONG = 0xF0222D39;
    public static final int SURFACE_HOVER = 0xF0303D4B;
    public static final int SURFACE_PRESSED = 0xF01A222C;
    public static final int SURFACE_DISABLED = 0x92252C33;
    public static final int BORDER = 0x694F6474;
    public static final int BORDER_SOFT = 0x3BFFFFFF;
    public static final int BORDER_STRONG = 0xAA62D6FF;
    public static final int FOCUS_RING = 0x5562D6FF;
    public static final int SHADOW = 0x78000000;
    public static final int SHADOW_SOFT = 0x36000000;
    public static final int HIGHLIGHT = 0x33FFFFFF;
    public static final int CONTROL_RED = 0xFFFF6B79;
    public static final int CONTROL_YELLOW = 0xFFFFC86A;
    public static final int CONTROL_GREEN = 0xFF67E08A;

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
        fillRounded(guiGraphics, x1 + 2, y1 + 4, x2 + 2, y2 + 4, radius, SHADOW);
        fillRounded(guiGraphics, x1 + 1, y1 + 2, x2 + 1, y2 + 2, radius, SHADOW_SOFT);
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        guiGraphics.fillGradient(x1 + 1, y1 + 1, x2 - 1, Math.min(y2 - 1, y1 + 18), HIGHLIGHT, alpha(0xFFFFFF, 0));
        guiGraphics.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, alpha(0xFFFFFF, 42));
        guiGraphics.fill(x1 + radius, y2 - 2, x2 - radius, y2 - 1, alpha(0x000000, 55));
    }

    public static void fillGlassPanel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius) {
        fillPanel(guiGraphics, x1, y1, x2, y2, radius, SURFACE_SOFT, BORDER);
        guiGraphics.fillGradient(x1 + 2, y1 + 2, x2 - 2, y2 - 2, alpha(0xFFFFFF, 18), alpha(0x62D6FF, 10));
    }

    public static void fillHeaderPanel(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius) {
        fillPanel(guiGraphics, x1, y1, x2, y2, radius, SURFACE_STRONG, BORDER);
        guiGraphics.fillGradient(x1 + 1, y1 + 1, x2 - 1, y1 + Math.min(28, Math.max(2, y2 - y1 - 1)),
                alpha(0xFFFFFF, 26), alpha(0x62D6FF, 18));
    }

    public static void fillContentGlow(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.fillGradient(x1, y1, x2, y2, BACKDROP_GLOW, alpha(0xFFC56E, 0));
    }

    public static void fillInset(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, boolean focused, boolean active) {
        if (focused && active) {
            fillRounded(guiGraphics, x1 - 1, y1 - 1, x2 + 1, y2 + 1, radius + 1, FOCUS_RING);
        }
        int border = !active ? 0x554D5760 : focused ? BORDER_STRONG : BORDER;
        int fill = !active ? SURFACE_DISABLED : 0xDD111922;
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        guiGraphics.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, alpha(0xFFFFFF, active ? 24 : 10));
        if (focused && active) {
            guiGraphics.fill(x1 + radius, y2 - 2, x2 - radius, y2 - 1, ACCENT_HOVER);
        }
    }

    public static void fillPill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int fill, int border) {
        fillRounded(guiGraphics, x1 + 1, y1 + 2, x2 + 1, y2 + 2, radius, SHADOW_SOFT);
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, border);
        fillRounded(guiGraphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fill);
        guiGraphics.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, alpha(0xFFFFFF, 32));
    }

    public static void fillSlot(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        fillRounded(guiGraphics, x - 2, y - 1, x + 18, y + 19, 5, SHADOW_SOFT);
        fillRounded(guiGraphics, x - 2, y - 2, x + 18, y + 18, 5, selected ? WARM : BORDER);
        fillRounded(guiGraphics, x - 1, y - 1, x + 17, y + 17, 4, 0xF0111820);
        guiGraphics.fill(x + 1, y, x + 15, y + 1, alpha(0xFFFFFF, 30));
        if (selected) {
            guiGraphics.fill(x + 2, y + 15, x + 14, y + 16, WARM);
        }
    }

    public static void fillSelection(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, boolean emphasized) {
        fillRounded(guiGraphics, x1, y1, x2, y2, radius, emphasized ? ACCENT_SOFT : alpha(0xFFFFFF, 22));
        guiGraphics.fill(x1 + 2, y1 + 2, x1 + 4, y2 - 2, emphasized ? ACCENT_HOVER : ACCENT);
    }

    public static void drawWindowControls(GuiGraphics guiGraphics, int x, int y) {
        fillRounded(guiGraphics, x, y, x + 6, y + 6, 3, CONTROL_RED);
        fillRounded(guiGraphics, x + 10, y, x + 16, y + 6, 3, CONTROL_YELLOW);
        fillRounded(guiGraphics, x + 20, y, x + 26, y + 6, 3, CONTROL_GREEN);
    }

    public static void fillItemWell(GuiGraphics guiGraphics, int centerX, int centerY, int size) {
        int half = size / 2;
        fillRounded(guiGraphics, centerX - half - 2, centerY - half, centerX + half + 2, centerY + half + 4, 8, SHADOW_SOFT);
        fillRounded(guiGraphics, centerX - half, centerY - half, centerX + half, centerY + half, 8, BORDER_SOFT);
        fillRounded(guiGraphics, centerX - half + 1, centerY - half + 1, centerX + half - 1, centerY + half - 1, 7, 0xC71A2430);
        guiGraphics.fill(centerX - half + 7, centerY - half + 2, centerX + half - 7, centerY - half + 3, HIGHLIGHT);
    }

    public static void fillSoftHalo(GuiGraphics guiGraphics, int centerX, int centerY, int width, int height, int color) {
        fillRounded(guiGraphics, centerX - width / 2, centerY - height / 2,
                centerX + width / 2, centerY + height / 2, Math.min(8, height / 2), color);
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
