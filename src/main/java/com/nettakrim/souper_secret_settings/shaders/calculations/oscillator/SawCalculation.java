package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import net.minecraft.util.Mth;

public class SawCalculation extends PeriodicCalculation {
    public SawCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return Mth.positiveModulo(t,1);
    }
}

