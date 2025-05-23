package com.nettakrim.souper_secret_settings.shaders.calculations.mix;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class MultiplyCalculation extends Calculation {
    public MultiplyCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"","",""};
    }

    @Override
    protected String[] getInputNames() {
        return new String[]{"a","b","center"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        // c changes whether a and c multiplies towards 0, or towards 1, same as lerp at c = 0.5, interestingly symmetrical
        float a = inputValues[0];
        float b = inputValues[1];
        float c = inputValues[2];
        outputValues[0] = -2*a*b*c + a*b + b*c + c*a;
    }
}
