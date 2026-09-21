package io.github.seraphina.infinity_item_editor_re.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

public final class MinecraftCompat {
    private static final char[] CHAT_FORMAT_CODES = "0123456789abcdefklmnor".toCharArray();

    private MinecraftCompat() {
    }

    public static @Nullable Screen screen(Minecraft minecraft) {
        return minecraft.gui.screen();
    }

    public static void setScreen(Minecraft minecraft, @Nullable Screen screen) {
        minecraft.gui.setScreen(screen);
    }

    public static boolean isSingleplayer(Minecraft minecraft) {
        return minecraft.hasSingleplayerServer();
    }

    public static char formattingCode(ChatFormatting formatting) {
        String serialized = formatting.toString();
        return serialized.length() > 1 ? serialized.charAt(1) : 'r';
    }

    public static ChatFormatting formattingByLegacyId(int id) {
        if (id < 0 || id >= 16) {
            return ChatFormatting.WHITE;
        }
        ChatFormatting formatting = ChatFormatting.getByCode(CHAT_FORMAT_CODES[id]);
        return formatting == null ? ChatFormatting.WHITE : formatting;
    }
}
