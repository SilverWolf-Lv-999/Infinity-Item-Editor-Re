package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    private static final String CATEGORY = "key.categories." + ModSource.MODID;

    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
            "key." + ModSource.MODID + ".open_editor",
            GLFW.GLFW_KEY_U,
            CATEGORY
    );
    public static final KeyMapping COPY_TARGET = new KeyMapping(
            "key." + ModSource.MODID + ".copy_target",
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    public static final KeyMapping SAVE_REALM = new KeyMapping(
            "key." + ModSource.MODID + ".save_realm",
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    private ClientKeyMappings() {
    }
}
