package io.github.seraphina.infinity_item_editor_re;

import com.mojang.logging.LogUtils;
import io.github.seraphina.infinity_item_editor_re.client.VoidConsumer;
import io.github.seraphina.infinity_item_editor_re.data.realms.RealmController;
import io.github.seraphina.infinity_item_editor_re.data.voids.VoidBuffer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ModSource {
    public static final String MODID = "infinity_item_editor_re";
    public static final String NAME = "Infinity Item Editor Re";
    public static final String VERSION = "1.20.1-1.1.0-R";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static File dataDir;
    public static RealmController realmController;
    public static final VoidBuffer voidBuffer = new VoidBuffer();

    private static boolean voidConsumerStarted;

    private ModSource() {
    }

    public static synchronized void initClientStorage(File minecraftDirectory) {
        Config.load(minecraftDirectory);
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

    public static synchronized void startVoidConsumer() {
        if (voidConsumerStarted) {
            return;
        }

        Thread voidThread = new Thread(new VoidConsumer(voidBuffer), "Infinity Item Editor Void Consumer");
        voidThread.setDaemon(true);
        voidThread.start();
        voidConsumerStarted = true;
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
