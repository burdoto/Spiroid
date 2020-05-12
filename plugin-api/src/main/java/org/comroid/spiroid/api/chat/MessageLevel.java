package org.comroid.spiroid.api.chat;

import org.bukkit.ChatColor;
import org.comroid.common.info.MessageSupplier;

import java.util.function.Function;

public interface MessageLevel extends Function<MessageSupplier, String> {
    ChatColor getStandardColor();

    ChatColor getHighlightColor();
}
