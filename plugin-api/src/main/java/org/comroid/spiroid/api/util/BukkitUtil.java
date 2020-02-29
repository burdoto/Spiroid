package org.comroid.spiroid.api.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class BukkitUtil {
    private BukkitUtil() {
    }
    
    public boolean unloadPlugin(Plugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);
        
        final ClassLoader classLoader = plugin.getClass().getClassLoader();
        
        try {
            ((Closeable) classLoader).close();
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Could not close plugin class loader", e);
        }
    }

    public static Optional<Player> getPlayer(CommandSender cmdSender) {
        if (cmdSender instanceof Player) return Optional.of((Player) cmdSender);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(cmdSender.getName()))
                return Optional.of(onlinePlayer);
        }

        return Optional.empty();
    }

    public static Optional<Material> getMaterial(@Nullable String name) {
        if (name == null) return Optional.empty();

        Material val = Material.getMaterial(name);
        if (val != null) return Optional.of(val);

        for (Material value : Material.values())
            if (value.name().equalsIgnoreCase(name))
                return Optional.of(value);
        return Optional.empty();
    }

    public static int getNumericPermissionValue(
            //Plugin.Permission permission,
            Permissible entity,
            Supplier<Integer> fallback
    ) {
        //if (entity.hasPermission(permission.node + "*"))
           // return Integer.MAX_VALUE;
        return entity.getEffectivePermissions()
                .stream()
               // .filter(perm -> perm.getPermission().indexOf(permission.node) == 0)
                .findFirst()
                .map(PermissionAttachmentInfo::getPermission)
                .map(str -> {
                    String[] split = str.split("\\.");
                    return split[split.length - 1];
                })
                .map(Integer::parseInt)
                .orElseGet(fallback);
    }

    public static long time2tick(long time, TimeUnit unit) {
        return unit.toSeconds(time) * 20; // TODO: 15.01.2020 Use current TPS instead of '20'
    }
}
