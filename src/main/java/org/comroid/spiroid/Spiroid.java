package org.comroid.spiroid;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.comroid.spiroid.api.AbstractPlugin;
import org.comroid.spiroid.api.command.SpiroidCommand;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public final class Spiroid extends AbstractPlugin {
    public static Spiroid INSTANCE;
    public static Logger LOGGER;

    public Spiroid() {
        super(SpiroidBaseCommand.values());
    }

    @Override
    public void load() {
        INSTANCE = this;
        LOGGER = getLogger();

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

    public enum SpiroidBaseCommand implements SpiroidCommand {
        spiroid {
            @Override
            public String execute(CommandSender sender, String[] args) {
                return "Spiroid running with Version " + Spiroid.INSTANCE.getVersion();
            }
        };

        private final SpiroidCommand[] subcommands;

        @Override
        public SpiroidCommand[] getSubcommands() {
            return subcommands;
        }

        SpiroidBaseCommand(SpiroidCommand... subcommands) {
            this.subcommands = subcommands;
        }

        @Override
        public String[] tabComplete(String startsWith) {
            return new String[0];
        }

        @Override
        public @Nullable String execute(CommandSender sender, String[] args) {
            return null;
        }
    }
}
