package com.nettakrim.souper_secret_settings.shaders;

@Deprecated
public class UniformDataOld<T> {
    public UniformDataOld(T value, T defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public T value;
    public T defaultValue;
}
