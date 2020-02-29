package org.comroid.spiroid.api.model;

import java.io.IOException;

import org.comroid.spiroid.api.exception.CycleException;

public interface Cyclable {
    void cycle() throws CycleException, IOException;

    /**
     * If {@link #cycle()} has failed before, this method runs instead with the thrown {@link Throwable}.
     * As soon as this method returns {@code true}, {@link #cycle()} will be invoked instead until it fails again.
     *
     * @param cause The cause of failure.
     *
     * @return Whether the problem is fixed.
     */
    boolean onFailure(Throwable cause);

    class Primitive implements Cyclable {
        private final Runnable task;

        public Primitive(Runnable task) {
            this.task = task;
        }

        @Override
        public void cycle() {
            task.run();
        }

        @Override
        public boolean onFailure(Throwable cause) {
            return true;
        }
    }
}
