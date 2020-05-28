package org.comroid.spiroid.api.command;

public @interface Command {
    String requiredPermission() default "";
}
