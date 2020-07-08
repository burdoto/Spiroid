package org.comroid.spiroid.api.command;

import org.bukkit.command.CommandSender;
import org.comroid.common.ref.Named;
import org.jetbrains.annotations.Nullable;

public interface SpiroidCommand extends Named {
    Object execute(CommandSender sender, boolean simulate, @Nullable String arg);
}
