package com.nettakrim.souper_secret_settings.actions;

import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniformValueChangedAction implements Action {
    private final String uniform;
    private final int i;

    private final LuminanceUniformOverride uniformOverride;
    private final MapConfig uniformConfig;

    private OverrideSource sourceBackup;
    private Map<String, List<Object>> mapBackup;

    public UniformValueChangedAction(String uniform, int i, LuminanceUniformOverride uniformOverride, MapConfig uniformConfig) {
        this.uniform = uniform;
        this.i = i;
        this.uniformOverride = uniformOverride;
        this.uniformConfig = uniformConfig;
    }

    @Override
    public void backup() {
        sourceBackup = uniformOverride.overrideSources.get(i);
        mapBackup = new HashMap<>(uniformConfig.config);
    }

    @Override
    public void undo() {
        uniformOverride.overrideSources.set(i, sourceBackup);
        uniformConfig.config.clear();
        uniformConfig.config.putAll(mapBackup);
    }

    @Override
    public boolean mergeWith(Action other) {
        UniformValueChangedAction o = (UniformValueChangedAction)other;
        return uniform.equals(o.uniform) && i == o.i;
    }
}
