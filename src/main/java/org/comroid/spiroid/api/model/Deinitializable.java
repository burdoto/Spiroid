package org.comroid.spiroid.api.model;

import org.comroid.spiroid.api.exception.DeinitializerException;

import java.io.IOException;

public interface Deinitializable {
    void deinitialize() throws DeinitializerException, IOException;
}
