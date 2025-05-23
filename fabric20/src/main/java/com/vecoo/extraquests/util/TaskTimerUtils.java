package com.vecoo.extraquests.util;

import com.vecoo.extraquests.ExtraQuests;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TaskTimerUtils {
    private final Consumer<TaskTimerUtils> consumer;
    private final long interval;
    private long currentIteration;
    private final long iterations;
    private long ticksRemaining;
    private boolean expired;

    private static final Set<TaskTimerUtils> tasks = new HashSet<>();

    private TaskTimerUtils(Consumer<TaskTimerUtils> consumer, long delay, long interval, long iterations) {
        this.consumer = consumer;
        this.interval = interval;
        this.iterations = iterations;
        this.ticksRemaining = delay;
    }

    public boolean isExpired() {
        return expired;
    }

    public void cancel() {
        this.expired = true;
        tasks.remove(this);
    }

    public static void cancelAll() {
        synchronized (tasks) {
            for (TaskTimerUtils task : new ArrayList<>(tasks)) {
                task.cancel();
            }
            tasks.clear();
        }
    }

    private void tick() {
        if (expired) {
            return;
        }

        if (--ticksRemaining > 0) {
            return;
        }

        consumer.accept(this);
        currentIteration++;

        if (iterations == -1 || currentIteration < iterations) {
            ticksRemaining = interval;
        } else {
            expired = true;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Consumer<TaskTimerUtils> consumer;
        private long delay;
        private long interval;
        private long iterations = 1;

        public Builder execute(@NotNull Runnable runnable) {
            this.consumer = task -> runnable.run();
            return this;
        }

        public Builder consume(@NotNull Consumer<TaskTimerUtils> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder delay(long delay) {
            if (delay < 0) {
                ExtraQuests.getLogger().error("[ExtraQuests] Delay must not be below 0");
                return null;
            }
            this.delay = delay;
            return this;
        }

        public Builder interval(long interval) {
            if (interval < 0) {
                ExtraQuests.getLogger().error("[ExtraQuests] Interval must not be below 0");
                return null;
            }
            this.interval = interval;
            return this;
        }

        public Builder iterations(long iterations) {
            if (iterations < -1) {
                ExtraQuests.getLogger().error("[ExtraQuests] Iterations must not be below -1");
                return null;
            }
            this.iterations = iterations;
            return this;
        }

        public Builder infinite() {
            return iterations(-1);
        }

        public TaskTimerUtils build() {
            if (consumer == null) {
                ExtraQuests.getLogger().error("[ExtraQuests] Consumer must be set");
                return null;
            }
            TaskTimerUtils task = new TaskTimerUtils(consumer, delay, interval, iterations);
            addTask(task);
            return task;
        }
    }

    private static synchronized void addTask(@NotNull TaskTimerUtils task) {
        tasks.add(task);
    }

    public static void onServerTick() {
        List<TaskTimerUtils> tasksCopy;

        synchronized (tasks) {
            tasksCopy = new ArrayList<>(tasks);
        }

        for (TaskTimerUtils task : tasksCopy) {
            task.tick();
            if (task.isExpired()) {
                tasks.remove(task);
            }
        }
    }
}