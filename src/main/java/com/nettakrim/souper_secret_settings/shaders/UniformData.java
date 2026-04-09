package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.PerValueOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UniformData {
    public final @Nullable UniformData defaultValue;

    public PerValueOverride override;
    public UniformConfig config;

    public UniformData(@Nullable UniformData defaultValue, PerValueOverride override, UniformConfig config) {
        this.defaultValue = defaultValue;
        this.override = override;
        this.config = config;
    }

    public boolean isChanged() {
        if (defaultValue == null || !override.getStrings().equals(defaultValue.override.getStrings())) {
            return true;
        }

        MapConfig mapConfig = (MapConfig)config;

        if (mapConfig.config().isEmpty() && defaultValue.config == EmptyConfig.INSTANCE) {
            return false;
        }

        if (defaultValue.config instanceof MapConfig defaultMap) {
            if (!defaultMap.config().keySet().equals(mapConfig.config().keySet())) {
                return true;
            }

            for (String s : defaultMap.config().keySet()) {
                List<Object> defaultObjects = defaultMap.config().get(s);
                List<Object> currentObjects = mapConfig.config().get(s);
                if (defaultObjects.size() != currentObjects.size()) {
                    return true;
                }

                for (int i = 0; i < defaultObjects.size(); i++) {
                    Object defaultObject = defaultObjects.get(i);
                    Object currentObject = currentObjects.get(i);

                    if (defaultObject.equals(currentObject)) {
                        continue;
                    }

                    if (defaultObject instanceof Number defaultNumber && currentObject instanceof Number currentNumber) {
                        if (defaultNumber.doubleValue() != currentNumber.doubleValue()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }

}
