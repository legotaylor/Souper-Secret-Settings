package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;

import java.util.Optional;

public class MixOverrideSource implements OverrideSource {
    public float a;
    public float b;
    public OverrideSource overrideSource;

    public MixOverrideSource(float a, float b, OverrideSource overrideSource) {
        this.a = a;
        this.b = b;
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
        return ( 1.0f - t) * a + b * t;
    }

    @Override
    public String getString() {
        return "mix("+a+"/"+b+"/"+overrideSource.getString()+")";
    }
}
