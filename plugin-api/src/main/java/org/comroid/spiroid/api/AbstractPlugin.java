package org.comroid.spiroid.api;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.common.Version;
import org.comroid.common.io.FileHandle;
import org.comroid.common.upd8r.model.UpdateChannel;
import org.comroid.spiroid.api.cycle.Cyclable;
import org.comroid.spiroid.api.cycle.CycleHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    protected final CycleHandler cycleHandler;
    protected final Map<String, FileConfiguration> configs = new ConcurrentHashMap<>();
    protected @Nullable UpdateChannel updateChannel;

    public Optional<UpdateChannel> getUpdateChannel() {
        return Optional.ofNullable(updateChannel);
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public final @NotNull FileConfiguration getConfig() {
        return Objects.requireNonNull(getConfig("config"), "Could not get default configuration file");
    }

    protected AbstractPlugin() {
        pluginYML = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Objects.requireNonNull(this
                .getClassLoader()
                .getResourceAsStream("plugin.yml"), "Could not find plugin.yml!"))));
        cycleHandler = new CycleHandler(this);
        cycleHandler.accept(new Cyclable.Primitive(this::saveConfig));

        version = new Version(Optional.ofNullable(pluginYML.getString("version"))
                .orElseThrow(() -> new AssertionError("Version not found in plugin.yml!")));
        commandHandler = new CommandHandler();
        configDir = new FileHandle(configPathBase());

        configDir.validateDir();
    }

    public final @NotNull FileConfiguration getConfig(String name) {
        final FileHandle dir = new FileHandle(configPathBase());
        if (!dir.exists() && dir.mkdir())
            getLogger().fine("Created configuration directory: " + dir);
        else if (!dir.exists())
            throw new UnsupportedOperationException("Could not create configuration directory: " + dir);

        return configs.computeIfAbsent(name, key -> {
            try { // if there is no configuration; create it:
                final FileHandle file = configDir.createSubFile(key);

                if (!file.exists() && !file.createNewFile() /* create the file if necessary */)
                    throw new IOException("Could not create configuration file: " + file.getAbsolutePath());
                else {
                    final YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
                    getConfigDefaults(key).ifPresent(loaded::setDefaults);
                    return loaded;
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not get config \"" + key + "\"", e);
            }

            throw new AssertionError("Could not compute configuration " + key);
        });
    }

    protected final String configPathBase() {
        // spigot is asking users to use the server root dir as the working directory
        return "plugins/" + getName();
    }

    @Override
    public final void reloadConfig() {
        getLogger().info("Reloading from configuration files...");

        configs.forEach((name, config) -> {
            try {
                final FileHandle file = configDir.createSubFile(name + ".yml");
                System.out.println("file1 = " + file);
                config.load(file);
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
                System.out.println("file2 = " + file);
                config.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                getLogger().log(Level.SEVERE, "Error saving config \"" + name + "\"", e);
            }
        });
    }

    @Override
    public final void saveDefaultConfig() throws UnsupportedOperationException {
        configs.computeIfPresent("config", (name, config) -> {
            try {
                final FileHandle file = configDir.createSubFile(name + ".yml");
                System.out.println("file3 = " + file);
                config.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                getLogger().log(Level.SEVERE, "Error saving default configuration", e);
            }

            return null; // voided
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return super.onCommand(sender, command, label, args); // TODO: 15.01.2020 Command Framework
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args); // TODO: 15.01.2020 Command Framework
    }

    @Override
    public @Nullable PluginCommand getCommand(@NotNull String name) {
        return super.getCommand(name); // TODO: 15.01.2020 Command Framework
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        getServer().getScheduler().scheduleSyncDelayedTask(this, cycleHandler);
    }

    protected Optional<MemoryConfiguration> getConfigDefaults(String name) {
        return Optional.empty();
    }
}
