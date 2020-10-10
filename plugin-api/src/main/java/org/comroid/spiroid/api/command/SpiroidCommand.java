package org.comroid.spiroid.api.command;

import org.bukkit.command.CommandSender;
import org.comroid.api.Named;
import org.comroid.common.ref.StaticCache;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SpiroidCommand extends Named {
    @Override
    String getName();

    SpiroidCommand[] getSubcommands();

    @NonExtendable
    default Map<String, SpiroidCommand> getSubcommandsOrdered() {
        return Collections.unmodifiableMap(
                StaticCache.access(this, "ordered", () -> {
                    final TrieMap<String, SpiroidCommand> map = TrieMap.ofString();
                    for (SpiroidCommand cmd : getSubcommands())
                        map.put(cmd.getName(), cmd);
                    return map;
                })
        );
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

    @Internal
    default boolean wrapExecution(CommandSender sender, String[] args, int index) {
        return findSubcommand(args, index)
                .map(cmd -> {
                    final String result = cmd.execute(sender, args);

                    if (result != null && !result.isEmpty())
                        sender.sendMessage(result);

                    return result != null;
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
