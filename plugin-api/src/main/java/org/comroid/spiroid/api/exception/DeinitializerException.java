package org.comroid.spiroid.api.exception;

public class DeinitializerException extends RuntimeException {
    public DeinitializerException(Throwable cause, String format, Object... args) {
        this(String.format(format, args), cause);
    }

    public DeinitializerException(String format, Object... args) {
        this(String.format(format, args));
    }

    public DeinitializerException(String message) {
        super(message);
    }

    public DeinitializerException(Throwable cause, String message) {
        super(message, cause);
    }
}
