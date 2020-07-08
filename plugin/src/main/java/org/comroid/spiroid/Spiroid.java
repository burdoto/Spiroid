package org.comroid.spiroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.configuration.MemoryConfiguration;
import org.comroid.common.Version;
import org.comroid.common.upd8r.nginx.NGinXUpdateChannel;
import org.comroid.spiroid.api.AbstractPlugin;
import org.comroid.spiroid.api.exception.PluginEnableException;
import org.comroid.spiroid.api.util.BukkitUtil;
import org.comroid.spiroid.api.util.WorldUtil;
import org.comroid.spiroid.chat.Chat;
import org.comroid.spiroid.chat.MessageType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Spiroid extends AbstractPlugin {
    public static Spiroid INSTANCE;
    public static Logger LOGGER;
    public static Const CONST;

    public Spiroid() {
        this.updateChannel = new NGinXUpdateChannel(
                this,
                "https://cdn.comroid.org/plugin/spiroid/plugin",
                filename -> new Version(filename.substring(filename.lastIndexOf('@'), filename.lastIndexOf('.'))),
                filename -> {
                    try {
                        return new URL("https://cdn.comroid.org/plugin/spiroid/plugin" + File.separatorChar + filename);
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                }
        );
    }

    public void updatePlugin(Plugin plugin) {
        if (plugin == this)
            throw new IllegalArgumentException("Spiroid cannot update itself");
        if (!(plugin instanceof AbstractPlugin))
            throw new IllegalArgumentException("Spiroid can only update Plugins that extend " + AbstractPlugin.class.getName());

        ((AbstractPlugin) plugin).getUpdateChannel().ifPresent(updateChannel -> {
            throw new UnsupportedOperationException(); //todo
        });
    }

    @Override
    public void load() {
        INSTANCE = this;
        LOGGER = getLogger();

        FileConfiguration config = getConfig("config");
        if (!config.isSet("defaults.claim-size"))
            config.set("defaults.claim-size", 128);
        if (!config.isSet("excluded-worlds"))
            config.set("excluded-worlds", new ArrayList<>());
        cycle();

        saveDefaultConfig();
        configs.put("config", getConfig("config"));
    }

    @Override
    public void disable() {
    }

    @Override
    public void enable() {
        try {
            CONST = new Const();
            org.bukkit.plugin.Plugin worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");
            org.bukkit.plugin.Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (worldEdit != null && worldGuard != null) {
                LOGGER.info("Detected WorldEdit and WorldGuard! Forcing WorldEdit and WorldGuard enabling...");
                worldEdit.getPluginLoader().enablePlugin(worldEdit);
                worldGuard.getPluginLoader().enablePlugin(worldGuard);
                LOGGER.info("Finished loading WorldEdit and WorldGuard. Removing WorldGuard SignChangeEvent listener...");
                SignChangeEvent.getHandlerList().unregister(worldGuard);
                LOGGER.info("Disabled WorldGuard SignChangeEvent listener! Continuing enabling...");
            }

            World configVersion = Bukkit.getWorld("configVersion");
            if (configVersion != null)
                Spiroid.LOGGER.warning("World with name \"configVersion\" detected. This world will be ignored by e2uClaim.");


            String excluded = getConfig("config")
                    .getStringList("excluded-worlds")
                    .stream()
                    .map(str -> {
                        if (Bukkit.getWorld(str) == null)
                            return str + " [invalid world name]";
                        return str;
                    })
                    .collect(Collectors.joining(", "));
            if (!excluded.isEmpty())
                LOGGER.info("Excluded worlds: " + excluded);

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::cycle,
                    BukkitUtil.time2tick(10, TimeUnit.SECONDS),
                    BukkitUtil.time2tick(5, TimeUnit.MINUTES));
        } catch (PluginEnableException e) {
            LOGGER.severe("Unable to load " + toString() + ": " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void cycle() {
        LOGGER.fine("Running plugin cycle...");

    }


    public final static class Const {
        public final String VERSION;

        private Const() {
            try {
                YamlConfiguration yml = new YamlConfiguration();
                yml.load(new InputStreamReader(
                        Objects.requireNonNull(Const.class.getClassLoader().getResourceAsStream("plugin.yml"),
                                "Could not access plugin.yml")));

                VERSION = yml.getString("version");
            } catch (IOException | InvalidConfigurationException e) {
                throw new AssertionError("Unexpected Exception", e);
            }
        }
    }

    public enum Permission {
        // Usage Permissions
        LOCK_USE("e2uclaim.lock", "You are not allowed to create locks!"),
        LOCK_OVERRIDE("e2uclaim.mod.lock", ""),
        CLAIM_USE("e2uclaim.claim", "You are not allowed to create claims!"),
        CLAIM_OVERRIDE("e2uclaim.mod.claim", ""),

        ADMIN("e2uclaim.admin"),

        // Numeric Permissions prefix
        CLAIM_SIZE("e2uclaim.claim.size.", "");

        @NotNull public final String node;
        @Nullable public final String customMissingMessage;

        Permission(@NotNull String node, @Nullable String customMissingMessage) {
            this.node = node;
            this.customMissingMessage = customMissingMessage;
        }

        Permission(@NotNull String node) {
            this(node, null);
        }

        public boolean check(CommandSender user) {
            if (user.hasPermission(node)) return true;
            if (customMissingMessage == null)
                Chat.message(user, MessageType.ERROR, "You are missing the required permission: %s", node);
            else if (customMissingMessage.isEmpty()) return false;
            else Chat.message(user, MessageType.ERROR, customMissingMessage);
            return false;
        }

        public boolean check(CommandSender user, String customMissingMessage) {
            if (user.hasPermission(node)) return true;
            if (customMissingMessage == null)
                Chat.message(user, MessageType.ERROR, "You are missing the required permission: %s", node);
            else if (customMissingMessage.isEmpty()) return false;
            else Chat.message(user, MessageType.ERROR, customMissingMessage);
            return false;
        }
    }
}
