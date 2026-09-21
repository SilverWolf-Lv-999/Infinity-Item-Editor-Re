package io.github.seraphina.infinity_item_editor_re.client;

import io.github.seraphina.infinity_item_editor_re.ModSource;

import java.io.File;

public final class ClientBootstrap {
    private static boolean voidConsumerStarted;

    private ClientBootstrap() {
    }

    public static void initialize(File minecraftDirectory) {
        ModSource.initClientStorage(minecraftDirectory);
        startVoidConsumer();
    }

    private static synchronized void startVoidConsumer() {
        if (voidConsumerStarted) {
            return;
        }

        Thread voidThread = new Thread(new VoidConsumer(ModSource.voidBuffer), "Infinity Item Editor Void Consumer");
        voidThread.setDaemon(true);
        voidThread.start();
        voidConsumerStarted = true;
    }
}
