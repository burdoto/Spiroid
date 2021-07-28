package org.comroid.spiroid.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.comroid.api.Named;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SpiroidCommand extends Named {
    SpiroidCommand[] getSubcommands();

    @NonExtendable
    default Map<String, SpiroidCommand> getSubcommandsOrdered() {
        final Map<String, SpiroidCommand> map = new ConcurrentHashMap<>();
        for (SpiroidCommand cmd : getSubcommands())
            map.put(cmd.getName(), cmd);
        return map;
    }

    /**
     * Tab completion for JUST command arguments.
     * Output is filtered by whether it {@linkplain String#startsWith(String) starts with}
     * the parameter {@code startsWith}.
     *
     * @param startsWith The filter to apply
     * @return An array of Strings containing all argumentative completions for this exact command.
     */
    String[] tabComplete(String startsWith);

    default List<String> getTabCompletions(String startsWith) {
        return Stream.concat(
                Stream.of(tabComplete(startsWith)),
                Stream.of(getSubcommands()).map(SpiroidCommand::getName)
        )
                //.filter(str -> str.startsWith(startsWith))
                .collect(Collectors.toList());
    }

    @Nullable
    String execute(CommandSender sender, String[] args);

    default boolean allowConsoleExecution() {
        return true;
    }

    @Internal
    default boolean wrapExecution(CommandSender sender, String[] args, int index) {
        return findSubcommand(args, index)
                .map(cmd -> {
                    try {
                        if (sender instanceof ConsoleCommandSender && !cmd.allowConsoleExecution())
                            throw new UnsupportedOperationException("Cannot execute this command from console");

                        final String result = cmd.execute(sender, args);

                        if (result != null && !result.isEmpty())
                            sender.sendMessage(result);

                        return result != null;
                    } catch (Throwable t) {
                        sender.sendMessage(ChatColor.RED + "An internal " + t.getClass().getSimpleName() + " occurred: " + t.getMessage());
                        return false;
                    }
                })
                .orElse(false);
    }

    @Internal
    default Optional<SpiroidCommand> findSubcommand(String[] args, int index) {
        if (args.length <= index)
            // return this if index is reached; no actual arguments are given
            return Optional.of(this);
        final SpiroidCommand next = getSubcommandsOrdered().get(args[index]);
        if (next == null)
            // return this if subcommand not found bcs it could be arguments
            return Optional.of(this);

        return Optional.of(next)
                .flatMap(sub -> {
                    if (sub.getSubcommands().length == 0)
                        return Optional.of(sub);
                    return sub.findSubcommand(args, index + 1);
                });
    }
}
