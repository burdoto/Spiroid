package org.comroid.spiroid.api;

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

import org.comroid.common.Version;
import org.comroid.common.upd8r.model.UpdateChannel;
import org.comroid.spiroid.api.model.Cyclable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlugin extends JavaPlugin implements Version.Container {
    public static AbstractPlugin instance;

    public final YamlConfiguration pluginYML;
    public final CycleHandler cycleHandler;
    public final Version version;
    
    protected @Nullable UpdateChannel updateChannel;

    protected Map<String, FileConfiguration> configs = new ConcurrentHashMap<>();

    {
        pluginYML = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Objects.requireNonNull(this
            .getClassLoader()
            .getResourceAsStream("plugin.yml"), "Could not find plugin.yml!"))));
        cycleHandler = new CycleHandler(this);
        cycleHandler.accept(new Cyclable.Primitive(this::saveConfig));
        
        version = new Version(Optional.ofNullable(pluginYML.getString("version"))
                .orElseThrow(() -> new AssertionError("Version not found in plugin.yml!")));
    }
    
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

    public final @Nullable FileConfiguration getConfig(String name) {
        final File dir = new File(configPathBase());
        if (!dir.exists())
            if (dir.mkdir())
                getLogger().fine("Created configuration directory: " + dir.getAbsolutePath());

        return configs.compute(name, (k, v) -> {
            if (v != null) return v; // if configuration is already set, return it

            try { // if there is no configuration; create it:
                File file = new File(dir.getAbsolutePath() + (dir.getAbsolutePath().endsWith("/") ? "" : '/') + name + ".yml"); // get the file
                if (!file.exists() && file.createNewFile() /* create the file if necessary */)
                    throw new IOException("Could not create configuration file: " + file.getAbsolutePath());

                return YamlConfiguration.loadConfiguration(file);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not get config \"" + name + "\"", e);
            }

            return null;
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
                File file = new File(configPathBase() + name + ".yml");
                config.load(name);
            } catch (InvalidConfigurationException | IOException e) {
                getLogger().log(Level.SEVERE, "Error reloading config \"" + name + "\"", e);
            }
        });
    }

    @Override
    public final void saveConfig() {
        getLogger().fine("Saving configuration files...");

        configs.forEach((name, config) -> {
            try {
                File file = new File(configPathBase() + name + ".yml");
                config.save(file);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error saving config \"" + name + "\"", e);
            }
        });
    }

    @Override
    public final void saveDefaultConfig() throws UnsupportedOperationException {
        configs.computeIfPresent("config", (name, config) -> {
            try {
                File file = new File(configPathBase() + name + ".yml");
                config.save(file);
            } catch (IOException e) {
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
}
