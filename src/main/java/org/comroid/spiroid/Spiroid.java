package org.comroid.spiroid;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.comroid.spiroid.api.AbstractPlugin;

import java.util.ArrayList;
import java.util.logging.Logger;

public final class Spiroid extends AbstractPlugin {
    public static Spiroid INSTANCE;
    public static Logger LOGGER;

    @Override
    public void load() {
        INSTANCE = this;
        LOGGER = getLogger();

        FileConfiguration config = getConfig("config");
        if (!config.isSet("defaults.claim-size"))
            config.set("defaults.claim-size", 128);
        if (!config.isSet("excluded-worlds"))
            config.set("excluded-worlds", new ArrayList<>());

        configs.put("config", getConfig("config"));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();

        if (player.isSprinting() && player.hasGravity()) {
            player.teleport(player.getLocation().add(0, 0.6, 0));
            player.setGliding(true);
        }
    }
}
