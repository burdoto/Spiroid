package org.comroid.spiroid.api.exception;

public class CycleException extends RuntimeException {
    public CycleException(Throwable cause, String format, Object... args) {
        this(String.format(format, args), cause);
    }

    public CycleException(String format, Object... args) {
        this(String.format(format, args));
    }

    public CycleException(String message) {
        super(message);
    }

    public CycleException(Throwable cause, String message) {
        super(message, cause);
    }
}
