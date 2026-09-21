package io.github.seraphina.infinity_item_editor_re.eventhandlers;

import io.github.seraphina.infinity_item_editor_re.ModSource;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

public final class ClientKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(ModSource.MODID, "main")
    );

    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
            "key." + ModSource.MODID + ".open_editor",
            InputConstants.KEY_U,
            CATEGORY
    );
    public static final KeyMapping COPY_TARGET = new KeyMapping(
            "key." + ModSource.MODID + ".copy_target",
            InputConstants.KEY_V,
            CATEGORY
    );
    public static final KeyMapping SAVE_REALM = new KeyMapping(
            "key." + ModSource.MODID + ".save_realm",
            InputConstants.KEY_G,
            CATEGORY
    );

    private ClientKeyMappings() {
    }
}
