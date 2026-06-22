package io.github.seraphina.infinity_item_editor_re;

import com.mojang.logging.LogUtils;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidBuffer;
import io.github.seraphina.infinity_item_editor_re.init.CreativeTabRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mod(ModSource.MODID)
public class ModSource {
    public static final String MODID = "infinity_item_editor_re";
    public static final String NAME = "Infinity Item Editor Re";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static File dataDir;
    public static RealmController realmController;
    public static final VoidBuffer voidBuffer = new VoidBuffer();

    public ModSource(IEventBus modEventBus, ModContainer modContainer) {
        CreativeTabRegistry.CREATIVE_TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            io.github.seraphina.infinity_item_editor_re.client.ConfigScreenRegistration.register(modContainer);
        }
    }

    public static synchronized void initClientStorage(File minecraftDirectory) {
        dataDir = new File(minecraftDirectory, "infinity-data");
        ensureDirectory(dataDir);
        ensureDirectory(new File(dataDir, "void"));
        migrateOldRealmFile();
        realmController = new RealmController(dataDir);
    }

    public static synchronized RealmController getRealmController() {
        return realmController;
    }

    public static synchronized RealmController getOrCreateRealmController(File minecraftDirectory) {
        if (realmController == null) {
            initClientStorage(minecraftDirectory);
        }

        return realmController;
    }

    private static void ensureDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.warn("Failed to create directory {}", directory.getAbsolutePath());
        }
    }

    private static void migrateOldRealmFile() {
        File oldRealmFile = new File(dataDir, "infinity.nbt");
        File newRealmFile = new File(dataDir, "realm.nbt");

        if (!oldRealmFile.exists()) {
            return;
        }

        if (newRealmFile.exists()) {
            LOGGER.warn("Found old realm file {}, but {} already exists.", oldRealmFile.getName(), newRealmFile.getName());
            return;
        }

        try {
            Files.move(oldRealmFile.toPath(), newRealmFile.toPath());
            LOGGER.info("Renamed old realm file {} to {}.", oldRealmFile.getName(), newRealmFile.getName());
        } catch (IOException exception) {
            LOGGER.error("Failed to migrate old realm file {}", oldRealmFile.getAbsolutePath(), exception);
        }
    }
}
