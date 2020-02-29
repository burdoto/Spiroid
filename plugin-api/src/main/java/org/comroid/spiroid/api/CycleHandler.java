package org.comroid.spiroid.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.comroid.spiroid.api.model.Cyclable;

import org.bukkit.Bukkit;

import static org.comroid.spiroid.api.util.BukkitUtil.time2tick;

public final class CycleHandler implements Consumer<Cyclable> {
    private final static String logPrefix = "<CycleHandler> ";

    private final AbstractPlugin plugin;
    private final Engine engine;
    private final Map<Cyclable, Throwable> cyclables;

    private long interval = time2tick(5, TimeUnit.MINUTES);

    CycleHandler(AbstractPlugin plugin) {
        this.plugin = plugin;
        this.engine = new Engine();
        this.cyclables = new ConcurrentHashMap<>(1);

        engine.reschedule();
    }

    public void setInterval(long time, TimeUnit unit) {
        this.interval = time2tick(time, unit);
    }

    @Override
    public void accept(Cyclable target) {
        cyclables.put(target, null);
    }

    protected class Engine implements Runnable {
        @Override
        public void run() {
            Map<Cyclable, Throwable> unfixed = new HashMap<>();
            Collection<Cyclable> removeThrowable = new ArrayList<>();

            cyclables.forEach((cyclable, throwable) -> {
                if (throwable == null) {
                    try {
                        cyclable.cycle();
                    } catch (Throwable e) {
                        plugin.getLogger().log(Level.WARNING, logPrefix + "An error occurred in " + cyclable, e);

                        if (!cyclable.onFailure(e))
                            unfixed.put(cyclable, e);
                        else removeThrowable.add(cyclable);
                    }
                } else {
                    if (!cyclable.onFailure(throwable))
                        unfixed.put(cyclable, throwable);
                    else removeThrowable.add(cyclable);
                }
            });

            cyclables.replaceAll((cyclable, throwable) -> unfixed.get(cyclable));
            removeThrowable.forEach(cyclable -> cyclables.replace(cyclable, null));

            reschedule();
        }

        protected final void reschedule() {
            Bukkit.getScheduler().runTaskLater(plugin, engine, interval);

            plugin.getLogger().finest(logPrefix + "Rescheduled Cycles with interval " + interval);
        }
    }
}
