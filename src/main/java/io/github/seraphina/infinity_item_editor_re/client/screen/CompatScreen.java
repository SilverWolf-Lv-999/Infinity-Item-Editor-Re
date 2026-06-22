package io.github.seraphina.infinity_item_editor_re.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class CompatScreen extends Screen {
    protected CompatScreen(Component title) {
        super(title);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(), event.modifiers());
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(new CharacterEvent(codePoint, modifiers));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return mouseReleased(event.x(), event.y(), event.button());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)));
    }

    public static boolean hasShiftDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean hasControlDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean hasAltDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
    }

    public static boolean isSelectAll(int keyCode) {
        return hasControlDown() && keyCode == GLFW.GLFW_KEY_A;
    }

    public static boolean isCopy(int keyCode) {
        return hasControlDown() && keyCode == GLFW.GLFW_KEY_C;
    }

    public static boolean isPaste(int keyCode) {
        return hasControlDown() && keyCode == GLFW.GLFW_KEY_V;
    }

    public static boolean isCut(int keyCode) {
        return hasControlDown() && keyCode == GLFW.GLFW_KEY_X;
    }
}
