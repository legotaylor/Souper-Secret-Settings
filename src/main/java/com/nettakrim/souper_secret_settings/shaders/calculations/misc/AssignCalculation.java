package com.nettakrim.souper_secret_settings.shaders.calculations.misc;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class AssignCalculation extends Calculation {
    public AssignCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"0.0"};
    }

    @Override
    protected String[] getInputNames() {
        return new String[]{"="};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        outputValues[0] = inputValues[0];
    }
}
