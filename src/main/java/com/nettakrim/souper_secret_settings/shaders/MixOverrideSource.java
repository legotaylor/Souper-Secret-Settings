package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.DefaultableConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;

import java.util.ArrayList;
import java.util.List;

public class MixOverrideSource implements OverrideSource {
    public OverrideSource overrideSource;

    public MixOverrideSource(OverrideSource overrideSource) {
        this.overrideSource = overrideSource;
    }

    @Override
    public Float get(UniformConfig uniformConfig, ShaderTime shaderTime) {
        Float t = overrideSource.get(uniformConfig, shaderTime);
        if (t == null) return null;

        List<Object> range = uniformConfig.getObjects("soup_range");
        float a = 0;
        float b = 1;
        if (range != null && range.size() >= 2) {
            a = ((Number)range.get(0)).floatValue();
            b = ((Number)range.get(1)).floatValue();
        }
        return ( 1.0f - t) * a + b * t;
    }

    @Override
    public String getString() {
        return overrideSource.getString();
    }

    @Override
    public UniformConfig getTemplateConfig() {
        return new DefaultableConfig(overrideSource.getTemplateConfig(), template);
    }

    private static final UniformConfig template = new MapConfig(List.of(new ConfigData("soup_range", new ArrayList<>(List.of(0, 1)))));

    public static OverrideSource MixParameterSourceFromString(String value) {
        OverrideSource overrideSource = ParameterOverrideSource.parameterSourceFromString(value);
        if (!value.isEmpty() && overrideSource instanceof ParameterOverrideSource) {
            return new MixOverrideSource(overrideSource);
        }
        return overrideSource;
    }
}
