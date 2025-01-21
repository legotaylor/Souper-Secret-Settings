package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

import java.util.List;

public abstract class PeriodicCalculation extends Calculation {
    protected boolean useConfig;

    public PeriodicCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"luminance_time", "1", "0"};
    }

    @Override
    protected String[] getInputNames() {
        return new String[]{"time", "period", "phase"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void updateInputValues() {
        for (int i = 1; i < inputs.length; i++) {
            updateInputValue(i);
        }

        OverrideSource overrideSource = inputs[0];
        assert overrideSource != null;

        useConfig = overrideSource.getTemplateConfig().getNames().contains("period");
        Float f = overrideSource.get(useConfig ? new MapConfig(List.of(new ConfigData("period", List.of(inputValues[1])))) : EmptyConfig.INSTANCE, Uniforms.shaderTime);
        if (f == null) return;

        inputValues[0] = f;
    }

    @Override
    protected void calculateOutputValues() {
        outputValues[0] = periodicCalculation(inputValues[0] / (useConfig ? 1 : inputValues[1]) + inputValues[2]);
    }

    protected abstract float periodicCalculation(float t);
}
