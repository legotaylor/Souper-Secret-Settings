package com.nettakrim.souper_secret_settings.shaders.calculations.persistent;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class HoldCalculation extends Calculation {
    public HoldCalculation(String id) {
        super(id);
    }

    float current;

    @Override
    protected String[] getInputs() {
        return new String[]{"","luminance_time","0.9"};
    }

    @Override
    protected String[] getInputNames() {
        return new String[] {"a > b","a","b"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        if (inputValues[1] >= inputValues[2]) {
            current = inputValues[0];
        }
        outputValues[0] = current;
    }
}
