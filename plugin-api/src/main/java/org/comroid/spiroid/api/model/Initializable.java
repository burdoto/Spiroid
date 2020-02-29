package org.comroid.spiroid.api.model;

import java.io.IOException;

import org.comroid.spiroid.api.exception.InitializerException;

public interface Initializable {
    void initialize() throws InitializerException, IOException;
}
