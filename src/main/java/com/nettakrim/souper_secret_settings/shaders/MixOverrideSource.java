package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.DefaultableConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixOverrideSource implements OverrideSource {
    public OverrideSource overrideSource;

    public MixOverrideSource(OverrideSource overrideSource) {
        this.overrideSource = overrideSource;
    }

    @Override
    public Float get(UniformConfig uniformConfig, ShaderTime shaderTime) {
        Float t = overrideSource.get(uniformConfig, shaderTime);
        if (t == null) return null;
        if (overrideSource instanceof UniformSource uniformSource) {
            Optional<UniformValue> min = uniformSource.getUniform().getMin();
            Optional<UniformValue> max = uniformSource.getUniform().getMax();
            if (min.isPresent() && max.isPresent()) {
                float minValue = min.get().values.getFirst();
                float maxValue = max.get().values.getFirst();
                t = (t - minValue) / (maxValue - minValue);
            }
        }

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
        return "mix("+overrideSource.getString()+")";
    }

    @Override
    public UniformConfig getTemplateConfig() {
        return new DefaultableConfig(overrideSource.getTemplateConfig(), template);
    }

    private static final UniformConfig template = new MapConfig(List.of(new ConfigData("soup_range", new ArrayList<>(List.of(0, 1)))));
}
