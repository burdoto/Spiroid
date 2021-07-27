package org.comroid.spiroid.chat;

import org.bukkit.ChatColor;

import java.util.function.Function;

public interface MessageLevel extends Function<String, String> {
    ChatColor getStandardColor();

    ChatColor getHighlightColor();
}
