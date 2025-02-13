package com.nettakrim.souper_secret_settings.shaders;

public interface Toggleable {
    boolean isActive();
    void setActive(boolean to);

    default void toggle() {
        setActive(!isActive());
    }
}
