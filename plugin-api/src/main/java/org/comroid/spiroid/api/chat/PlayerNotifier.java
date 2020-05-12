package org.comroid.spiroid.api.chat;

import org.bukkit.entity.Player;
import org.comroid.common.info.MessageSupplier;
import org.jetbrains.annotations.Contract;

public interface PlayerNotifier extends Notifier {
    Player getPlayer();

    @Override
    default void sendMessage(MessageLevel level, MessageSupplier messageSupplier) {
        getPlayer().sendMessage(level.apply(messageSupplier));
    }

    default TitleApi title(String title) {
        return new TitleApi(this, title);
    }

    static PlayerNotifier of(final Player player) {
        return () -> player;
    }

    final class TitleApi {
        private final PlayerNotifier playerNotifier;
        private final String title;
        private String subtitle;
        private int fadeIn;
        private int stay;
        private int fadeOut;

        private TitleApi(PlayerNotifier playerNotifier, String title) {
            this.playerNotifier = playerNotifier;
            this.title = title;
        }

        @Contract("_ -> this")
        public TitleApi subtitle(String subtitle) {
            this.subtitle = subtitle;

            return this;
        }

        public TitleApi time(int sec, int fade) {
            this.fadeIn = this.fadeOut = sec / fade;
            this.stay   = sec / (fade * 2);

            return this;
        }

        public TitleApi time(int fadeIn, int stay, int fadeOut) {
            this.fadeIn  = fadeIn;
            this.stay    = stay;
            this.fadeOut = fadeOut;

            return this;
        }

        public void send() {
            playerNotifier.getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }
}
