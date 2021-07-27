package org.comroid.spiroid.api;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.common.Version;
import org.comroid.common.io.FileHandle;
import org.comroid.mutatio.span.Span;
import org.comroid.spiroid.api.chat.PlayerNotifier;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.comroid.spiroid.api.util.BukkitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

public abstract class AbstractPlugin extends JavaPlugin implements Version.Container {
    public static final Class<? extends AbstractPlugin> spiroid;
    public static AbstractPlugin instance;

    static {
        try {
            //noinspection unchecked
            spiroid = (Class<? extends AbstractPlugin>) Class.forName("org.comroid.spiroid.Spiroid");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class unavailable: org.comroid.spiroid.Spiroid");
        }
    }

    public final YamlConfiguration pluginYML;
    public final Version version;
    public final FileHandle configDir;
    protected final Map<String, Configuration> configs = new ConcurrentHashMap<>();
    protected final Map<String, SpiroidCommand> commands = new ConcurrentHashMap<>();
    private final Span<String> configNames;

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public final @NotNull FileConfiguration getConfig() {
        return Objects.requireNonNull(getConfig("config"), "Could not get default configuration file");
    }

    protected AbstractPlugin(String... configNames) {
        this(new SpiroidCommand[0], configNames);
    }

    protected AbstractPlugin(SpiroidCommand[] baseCommands, String... configNames) {
        instance = this;

        if (baseCommands.length == 0)
            getLogger().log(Level.WARNING, "No command Handlers are defined");
        for (SpiroidCommand cmd : baseCommands)
            commands.put(cmd.getName(), cmd);

        this.configNames = Span.<String>make()
                .initialValues(configNames)
                .span();

        try (
                InputStream is = this.getClassLoader().getResourceAsStream("plugin.yml");
                InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is, "plugin.yml not found"))
        ) {
            this.pluginYML = YamlConfiguration.loadConfiguration(isr);
        } catch (IOException ioe) {
            throw new RuntimeException("Error reading plugin.yml", ioe);
        } catch (NullPointerException npe) {
            throw new AssertionError(npe);
        }

        this.version = new Version(Optional.ofNullable(pluginYML.getString("version"))
                .orElseThrow(() -> new AssertionError("Version not found in plugin.yml!")));
        this.configDir = new FileHandle(getDataFolder());
        configDir.validateDir();
    }

    public final @NotNull FileConfiguration getConfig(String name) {
        if (!configDir.exists() && configDir.mkdir())
            getLogger().fine("Created configuration directory: " + configDir);
        else if (!configDir.exists())
            throw new UnsupportedOperationException("Could not create configuration directory: " + configDir);

        return (FileConfiguration) configs.computeIfAbsent(name, key -> {
            try { // if there is no configuration; create it:
                final FileHandle file = configDir.createSubFile(key + ".yml");

                if (!file.exists() && !file.createNewFile() /* create the file if necessary */)
                    throw new IOException("Could not create configuration file: " + file.getAbsolutePath());
                else {
                    final YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
                    getConfigDefaults(key).ifPresent(loaded::setDefaults);

                    if (!configNames.contains(name))
                        configNames.add(name);

                    return loaded;
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not get config \"" + key + "\"", e);
            }

            throw new AssertionError("Could not compute configuration " + key);
        });
    }

    @Override
    public final void reloadConfig() {
        getLogger().info("Reloading from configuration files...");

        configs.forEach((name, config) -> {
            try {
                final FileHandle file = configDir.createSubFile(name + ".yml");

                if (config instanceof FileConfiguration)
                    ((FileConfiguration) config).load(file);
                else throw new AssertionError("Config is not FileConfiguration");
            } catch (InvalidConfigurationException | IOException e) {
                getLogger().log(Level.SEVERE, "Error reloading config \"" + name + "\"", e);
            }
        });
    }

    /**
     * Saves all cached configurations.
     */
    @Override
    public final void saveConfig() {
        getLogger().fine("Saving configuration files...");

        configs.forEach((name, config) -> {
            try {
                final FileHandle file = configDir.createSubFile(name + ".yml");

                if (config instanceof FileConfiguration)
                    ((FileConfiguration) config).save(file);
                else throw new AssertionError("Config is not FileConfiguration");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error saving config \"" + name + "\"", e);
            }
        });
    }

    @Override
    public final void saveDefaultConfig() throws UnsupportedOperationException {
        for (String configName : configNames) {
            try {
                FileConfiguration config = getConfig(configName);
                FileHandle file = configDir.createSubFile(configName + ".yml");
                config.save(file);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not save " + configName, e);
            }
        }
    }

    @Override
    public final void onLoad() {
        super.onLoad();
        load();
        saveDefaultConfig();
    }

    @Override
    public final void onEnable() {
        super.onEnable();
        enable();
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        disable();
        saveDefaultConfig();
    }

    @Override
    public final boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        final SpiroidCommand cmd = commands.get(label);

        if (cmd == null)
            return false;

        return cmd.wrapExecution(sender, args, 0);
    }

    @Override
    public final @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        final SpiroidCommand cmd = commands.get(label);

        if (cmd == null)
            return null;

        return cmd.findSubcommand(args, 0)
                .map(sub -> sub.getTabCompletions(args.length > 0 ? args[args.length - 1] : ""))
                .orElse(null);
    }

    public final CompletableFuture<Void> schedule(long time, TimeUnit unit, Runnable task) {
        return schedule(time, unit, () -> {
            task.run();
            return null;
        });
    }

    public final <T> CompletableFuture<T> schedule(long time, TimeUnit unit, Supplier<T> task) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        getServer().getScheduler()
                .runTaskLater(this,
                        () -> {
                            T var = null;
                            try {
                                var = task.get();
                            } catch (Throwable t) {
                                future.completeExceptionally(t);
                            } finally {
                                future.complete(var);
                            }
                        },
                        BukkitUtil.time2tick(time, unit));

        return future;
    }

    private final Map<UUID, PlayerNotifier> notifiers = new ConcurrentHashMap<>();

    public final PlayerNotifier getPlayerNotifier(final Player player) {
        return notifiers.computeIfAbsent(player.getUniqueId(), key -> PlayerNotifier.of(player));
    }

    protected void load() {
    }

    protected void enable() {
    }

    protected void disable() {
    }

    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        return Optional.empty();
    }
}
