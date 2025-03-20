package com.nettakrim.souper_secret_settings.shaders.calculations.persistent;

import com.mclegoman.luminance.client.shaders.Uniforms;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.util.math.MathHelper;

public class SmoothCalculation extends Calculation {
    public SmoothCalculation(String id) {
        super(id);
    }

    float current;

    @Override
    protected String[] getInputs() {
        return new String[]{"","1"};
    }

    @Override
    protected String[] getInputNames() {
        return new String[] {"input", "speed"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        current = MathHelper.lerp(Uniforms.shaderTime.getDeltaTime()*inputValues[1], current, inputValues[0]);
        outputValues[0] = current;
    }
}
