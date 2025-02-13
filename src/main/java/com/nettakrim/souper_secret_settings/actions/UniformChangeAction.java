package com.nettakrim.souper_secret_settings.actions;

import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniformChangeAction implements Action {
    private final String uniform;
    private final int i;

    private final LuminanceUniformOverride uniformOverride;
    private final MapConfig uniformConfig;

    private OverrideSource sourceBackup;
    private Map<String, List<Object>> mapBackup;

    public UniformChangeAction(String uniform, int i, LuminanceUniformOverride uniformOverride, MapConfig uniformConfig) {
        this.uniform = uniform;
        this.i = i;
        this.uniformOverride = uniformOverride;
        this.uniformConfig = uniformConfig;
    }

    @Override
    public boolean undo() {
        swap();
        return true;
    }

    @Override
    public void redo() {
        swap();
    }

    protected void swap() {
        sourceBackup = uniformOverride.overrideSources.set(i, sourceBackup);

        Map<String, List<Object>> prev = backup();
        String prefix = i+"_";
        uniformConfig.config.forEach((key, value) -> SouperSecretSettingsClient.log(prefix, key, value));
        uniformConfig.config.keySet().removeIf((s) -> s.startsWith(prefix));
        uniformConfig.config.putAll(mapBackup);
        mapBackup = prev;
    }

    @Override
    public boolean mergeWith(Action other) {
        UniformChangeAction o = (UniformChangeAction)other;
        return uniformOverride == o.uniformOverride && uniform.equals(o.uniform) && i == o.i;
    }

    @Override
    public void addToHistory() {
        if (SouperSecretSettingsClient.actions.addToHistory(this)) {
            sourceBackup = uniformOverride.overrideSources.get(i);
            mapBackup = backup();
        }
    }

    private Map<String, List<Object>> backup() {
        String prefix = i+"_";
        Map<String, List<Object>> map = new HashMap<>();
        uniformConfig.config.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                map.put(key, value);
            }
        });
        return map;
    }
}
