package org.comroid.spiroid.api.model;

import org.comroid.spiroid.api.exception.InitializerException;

import java.io.IOException;

public interface Initializable {
    void initialize() throws InitializerException, IOException;
}
