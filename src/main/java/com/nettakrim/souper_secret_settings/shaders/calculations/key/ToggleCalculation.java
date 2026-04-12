package com.nettakrim.souper_secret_settings.shaders.calculations.key;

public class ToggleCalculation extends KeyCalculation {
    public ToggleCalculation(String id) {
        super(id);
    }

    private float lastValue;
    private boolean active = true;

    @Override
    protected void calculateOutputValues() {
        float value = inputValues[0];

        if (value > 0.5 && lastValue <= 0.5) {
            active = !active;
        }
        lastValue = value;

        outputValues[0] = active ? 1.0f : 0.0f;
    }
}
