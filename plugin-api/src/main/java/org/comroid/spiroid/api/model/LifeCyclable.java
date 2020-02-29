package org.comroid.spiroid.api.model;

import java.io.IOException;

import org.comroid.spiroid.api.exception.CycleException;
import org.comroid.spiroid.api.exception.DeinitializerException;
import org.comroid.spiroid.api.exception.InitializerException;

public interface LifeCyclable extends BiInitializable, Cyclable {
    @Override
    void initialize() throws InitializerException, IOException;

    @Override
    void deinitialize() throws DeinitializerException, IOException;

    @Override
    void cycle() throws CycleException, IOException;

    @Override
    boolean onFailure(Throwable cause);
}
