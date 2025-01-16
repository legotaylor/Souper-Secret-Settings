package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;

import java.util.Optional;

public class ParameterOverrideSource implements OverrideSource {
    public UniformSource source;
    private Float lastValue = 0f;

    public ParameterOverrideSource(UniformSource source) {
        this.source = source;
    }

    @Override
    public Float get(UniformConfig uniformConfig, ShaderTime shaderTime) {
        ShaderStack stack = ShaderStack.getRenderingStack();
        if (stack != null) {
            String parameter = source.getString();
            if (!parameter.isEmpty() && stack.parameterValues.containsKey(parameter)) {
                lastValue = stack.parameterValues.get(parameter);
            } else {
                Float f = source.get(uniformConfig, shaderTime);
                if (f == null) return null;

                Optional<UniformValue> min = source.getUniform().getMin();
                Optional<UniformValue> max = source.getUniform().getMax();
                if (min.isPresent() && max.isPresent()) {
                    float minValue = min.get().values.getFirst();
                    float maxValue = max.get().values.getFirst();
                    f = (f - minValue) / (maxValue - minValue);
                }
                lastValue = f;
            }
        }

        return lastValue;
    }

    @Override
    public String getString() {
        return source.getString();
    }

    public static OverrideSource parameterSourceFromString(String s) {
        OverrideSource overrideSource = LuminanceUniformOverride.sourceFromString(s);
        if (overrideSource instanceof UniformSource uniformSource) {
            return new ParameterOverrideSource(uniformSource);
        }
        return overrideSource;
    }
}
