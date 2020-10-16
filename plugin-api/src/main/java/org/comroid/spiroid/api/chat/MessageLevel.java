package org.comroid.spiroid.api.chat;

import org.bukkit.ChatColor;
import org.comroid.common.info.MessageSupplier;

import java.util.function.Function;

public interface MessageLevel extends Function<String, String> {
    ChatColor getStandardColor();

    ChatColor getHighlightColor();
}
