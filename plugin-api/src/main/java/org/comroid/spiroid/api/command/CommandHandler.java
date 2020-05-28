package org.comroid.spiroid.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.List;
import java.util.function.Supplier;

public final class CommandHandler {
    public int registerCommands(CommandContainer... commandContainers) {
    }

    public boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    public PluginCommand getCommand(String name, Supplier<PluginCommand> bukkitEquivalentSupplier) {
        return null;
    }
}
