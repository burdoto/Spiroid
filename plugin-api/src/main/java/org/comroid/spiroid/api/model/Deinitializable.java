package org.comroid.spiroid.api.model;

import java.io.IOException;

import org.comroid.spiroid.api.exception.DeinitializerException;

public interface Deinitializable {
    void deinitialize() throws DeinitializerException, IOException;
}
