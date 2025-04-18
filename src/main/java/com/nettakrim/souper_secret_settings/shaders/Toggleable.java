package com.nettakrim.souper_secret_settings.shaders;

public interface Toggleable {
    boolean isActive();
    void setActive(boolean to);

    default void toggle() {
        setActive(!isActive());
    }

    class CallableToggle implements Toggleable {
        public Runnable runnable;

        public CallableToggle(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void setActive(boolean to) {
            runnable.run();
        }
    }
}
