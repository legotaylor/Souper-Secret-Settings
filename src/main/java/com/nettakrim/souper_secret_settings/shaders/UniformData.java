package com.nettakrim.souper_secret_settings.shaders;

public class UniformData<T> {
    public UniformData(T value, T defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public T value;
    public T defaultValue;
}
