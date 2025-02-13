package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;

import java.util.ArrayList;
import java.util.List;

public abstract class Calculation implements Toggleable {
    public final OverrideSource[] inputs;
    public final String[] inputNames;
    public final String[] outputs;

    protected final float[] inputValues;
    protected final float[] outputValues;

    private final String id;

    public boolean active = true;
    public boolean expanded = false;

    public Calculation(String id) {
        this.id = id;

        String[] inputStrings = getInputs();
        String[] names = getInputNames();

        this.inputs = new OverrideSource[inputStrings.length];
        this.inputNames = new String[inputStrings.length];
        for (int i = 0; i < inputs.length; i++) {
            this.inputs[i] = ParameterOverrideSource.parameterSourceFromString(inputStrings[i]);
            this.inputNames[i] = i < names.length ? names[i] : String.valueOf((char)('a'+i));
        }

        this.outputs = getOutputs();

        inputValues = new float[inputs.length];
        outputValues = new float[outputs.length];
    }

    protected abstract String[] getInputs();
    protected abstract String[] getInputNames();
    protected abstract String[] getOutputs();

    public void update(ShaderLayer layer) {
        if (!active) return;

        updateInputValues();
        calculateOutputValues();

        for (int i = 0; i < outputs.length; i++) {
            String output = outputs[i];
            if (!output.isBlank()) {
                layer.parameterValues.put(output, outputValues[i]);
            }
        }
    }

    protected void updateInputValues() {
        for (int i = 0; i < inputs.length; i++) {
            updateInputValue(i);
        }
    }

    protected void updateInputValue(int i) {
        OverrideSource overrideSource = inputs[i];
        assert overrideSource != null;

        Float f = overrideSource.get(EmptyConfig.INSTANCE, Uniforms.shaderTime);
        if (f == null) return;

        inputValues[i] = f;
    }

    protected abstract void calculateOutputValues();

    public String getID() {
        return id;
    }

    public List<Float> getLastOutput() {
        List<Float> list = new ArrayList<>(outputValues.length);
        for (float outputValue : outputValues) {
            list.add(outputValue);
        }
        return list;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean to) {
        active = to;
    }
}
