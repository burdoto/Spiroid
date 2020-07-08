package org.comroid.spiroid.api.command;

import org.bukkit.command.CommandSender;
import org.comroid.common.ref.Named;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SpiroidCommand extends Named {
    @Override
    String getName();

    SpiroidCommand[] getSubcommands();

    String execute(CommandSender sender, String[] args);

    String[] tabComplete(String filter);

    default List<String> getTabCompletions(String filter) {
        return Stream.concat(
                Stream.of(tabComplete(filter)),
                Stream.of(getSubcommands()).map(SpiroidCommand::getName)
        )
                .filter(str -> str.startsWith(filter))
                .collect(Collectors.toList());
    }
}
