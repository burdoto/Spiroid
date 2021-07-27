package org.comroid.spiroid.chat;

import org.comroid.api.Specifiable;

public interface Notifier extends Specifiable<Notifier> {
    void sendMessage(MessageLevel level, String message);
}
