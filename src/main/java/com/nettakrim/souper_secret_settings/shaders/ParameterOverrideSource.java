package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.DefaultableConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.ArrayList;
import java.util.List;

public class ParameterOverrideSource implements OverrideSource {
    public UniformSource source;
    private Float lastValue = 0f;

    public ParameterOverrideSource(UniformSource source) {
        this.source = source;
    }

    @Override
    public Float get(UniformConfig uniformConfig, ShaderTime shaderTime) {
        ShaderLayer layer = ShaderLayer.getRenderingLayer();
        if (layer == null) {
            layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        }

        if (layer != null) {
            String parameter = source.getString();
            if (!parameter.isEmpty() && layer.parameterValues.containsKey(parameter)) {
                lastValue = layer.parameterValues.get(parameter);
                List<Object> range = uniformConfig.getObjects("range");
                if (range != null && range.size() >= 2) {
                    lastValue = UniformSource.remapRange(lastValue, 0f, 1f, range.get(0), range.get(1));
                }
            } else {
                lastValue = source.get(uniformConfig, shaderTime);
            }
        }

        return lastValue;
    }

    @Override
    public String getString() {
        return source.getString();
    }

    @Override
    public UniformConfig getTemplateConfig() {
        UniformConfig config = source.getTemplateConfig();
        return new DefaultableConfig(config, template);
    }

    private static final UniformConfig template = new MapConfig(List.of(new ConfigData("range", new ArrayList<>(List.of(0.0f, 1.0f)))));

    public static OverrideSource parameterSourceFromString(String s) {
        OverrideSource overrideSource = LuminanceUniformOverride.sourceFromString(s);
        if (overrideSource instanceof UniformSource uniformSource) {
            return new ParameterOverrideSource(uniformSource);
        }
        return overrideSource;
    }
}
