package org.comroid.spiroid.api.model;

import java.io.IOException;

import org.comroid.spiroid.api.exception.DeinitializerException;
import org.comroid.spiroid.api.exception.InitializerException;

public interface BiInitializable extends Initializable, Deinitializable {
    @Override
    void initialize() throws InitializerException, IOException;

    @Override
    void deinitialize() throws DeinitializerException, IOException;
}
