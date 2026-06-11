package com.vecoo.extraquests.service;

import com.vecoo.extralib.loader.GsonLoader;
import com.vecoo.extralib.scheduler.TaskTimer;
import com.vecoo.extralib.util.WorldUtil;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.manager.ExtraQuestsManager;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Getter
public class PlayerService {
    @Nonnull
    private final Path filePath;
    @Nonnull
    private final Map<UUID, PlayerStorage> storage = new ConcurrentHashMap<>();
    @Nonnull
    private final Map<UUID, ReentrantLock> playerLocks = new ConcurrentHashMap<>();

    public PlayerService(@Nonnull String directory, @Nonnull MinecraftServer server) {
        this.filePath = Paths.get(WorldUtil.resolveWorldDirectory(directory, server));
    }

    @Nonnull
    public PlayerStorage getStorage(@Nonnull UUID playerUUID) {
        return this.storage.computeIfAbsent(playerUUID, uuid ->
                new PlayerStorage(uuid, new HashMap<>())
        );
    }

    public void modifyStorage(@Nonnull UUID playerUUID, Consumer<PlayerStorage> consumer) {
        val storage = getStorage(playerUUID);
        val lock = getLock(playerUUID);

        lock.lock();

        try {
            consumer.accept(storage);
            storage.getDirty().set(true);
        } finally {
            lock.unlock();
        }
    }

    public void save(boolean force) {
        for (PlayerStorage storage : this.storage.values()) {
            val lock = getLock(storage.getPlayerUUID());

            lock.lock();

            try {
                if (storage.getDirty().compareAndSet(true, false) || force) {
                    GsonLoader.save(storage.copy(), this.filePath.resolve(storage.getPlayerUUID() + ".json"));
                }
            } catch (IOException e) {
                storage.getDirty().set(true);
                ExtraQuests.getLogger().error(e.getMessage());
            } finally {
                lock.unlock();
            }
        }
    }

    private void saveInterval() {
        TaskTimer.builder()
                .delay(150 * 20L)
                .interval(150 * 20L)
                .infinite()
                .execute(() -> {
                    if (ExtraQuests.getInstance().getServer().isServerRunning()) {
                        for (PlayerStorage storage : this.storage.values()) {
                            PlayerStorage snapshot;
                            val lock = getLock(storage.getPlayerUUID());

                            lock.lock();

                            try {
                                if (!storage.getDirty().compareAndSet(true, false)) {
                                    continue;
                                }

                                snapshot = storage.copy();
                            } finally {
                                lock.unlock();
                            }

                            CompletableFuture.runAsync(() -> {
                                try {
                                    GsonLoader.save(snapshot, this.filePath.resolve(snapshot.getPlayerUUID() + ".json"));
                                } catch (IOException e) {
                                    storage.getDirty().set(true);
                                    ExtraQuests.getLogger().error("Async save error: ", e);
                                }
                            }, GsonLoader.WRITER_EXECUTOR);
                        }
                    }
                }).build();
    }

    public void init() throws IOException {
        if (!this.storage.isEmpty()) {
            return;
        }

        val list = this.filePath.toFile().listFiles((dir, name) -> name.endsWith(".json"));

        if (list == null) {
            return;
        }

        val time = System.currentTimeMillis();

        for (File file : list) {
            val storage = GsonLoader.load(PlayerStorage.class, file.toPath(), true);

            if (storage == null) {
                throw new IOException(String.format("Failed to load file: %s. Data reset, create backup.", file.toPath()));
            }

            val iterator = storage.getQuestTimers().entrySet().iterator();

            while (iterator.hasNext()) {
                val entry = iterator.next();

                if (entry.getValue() <= time) {
                    iterator.remove();
                    ExtraQuestsManager.questReset(storage.getPlayerUUID(), entry.getKey());
                } else {
                    ExtraQuestsManager.startQuestTimer(storage.getPlayerUUID(), entry.getKey(), entry.getValue());
                }
            }

            this.storage.put(storage.getPlayerUUID(), storage);
        }

        save(true);
        saveInterval();
    }

    @Nonnull
    private ReentrantLock getLock(@Nonnull UUID playerUUID) {
        return this.playerLocks.computeIfAbsent(playerUUID, uuid -> new ReentrantLock());
    }
}