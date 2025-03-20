package com.nettakrim.souper_secret_settings.shaders.calculations.persistent;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class DeltaCalculation extends Calculation {
    public DeltaCalculation(String id) {
        super(id);
    }

    float current;

    @Override
    protected String[] getInputs() {
        return new String[]{""};
    }

    @Override
    protected String[] getInputNames() {
        return new String[] {"input"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        outputValues[0] = inputValues[0] - current;
        current = inputValues[0];
    }
}
