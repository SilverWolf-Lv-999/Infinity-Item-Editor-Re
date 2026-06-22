package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            ResourceLocation.fromNamespaceAndPath(ModSource.MODID, "main")
    );

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
