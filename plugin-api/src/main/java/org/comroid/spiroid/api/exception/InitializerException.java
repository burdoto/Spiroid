package org.comroid.spiroid.api.exception;

public class InitializerException extends RuntimeException {
    public InitializerException(Throwable cause, String format, Object... args) {
        this(String.format(format, args), cause);
    }

    public InitializerException(String format, Object... args) {
        this(String.format(format, args));
    }

    public InitializerException(String message) {
        super(message);
    }

    public InitializerException(Throwable cause, String message) {
        super(message, cause);
    }
}
