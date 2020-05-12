package org.comroid.spiroid.api.chat;

import org.comroid.common.info.MessageSupplier;
import org.comroid.common.ref.Specifiable;

public interface Notifier extends Specifiable<Notifier> {
    void sendMessage(MessageLevel level, MessageSupplier messageSupplier);
}
