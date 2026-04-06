package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import net.minecraft.util.Mth;

public class SineCalculation extends PeriodicCalculation {
    public SineCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return (Mth.cos(t*Mth.TWO_PI)+1)/2f;
    }
}

