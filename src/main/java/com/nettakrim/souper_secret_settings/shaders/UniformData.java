package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.PerValueOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import org.jetbrains.annotations.Nullable;

public class UniformData {
    public final @Nullable UniformData defaultValue;

    public PerValueOverride override;
    public UniformConfig config;

    public UniformData(@Nullable UniformData defaultValue, PerValueOverride override, UniformConfig config) {
        this.defaultValue = defaultValue;
        this.override = override;
        this.config = config;
    }
}
