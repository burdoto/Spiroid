package org.comroid.spiroid.api.chat;

import org.comroid.api.Specifiable;
import org.comroid.common.info.MessageSupplier;

public interface Notifier extends Specifiable<Notifier> {
    void sendMessage(MessageLevel level, String message);
}
