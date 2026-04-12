package com.nettakrim.souper_secret_settings.shaders.calculations.key;

import java.util.HashSet;
import java.util.Set;

public class SliderCalculation extends KeyCalculation {
    public static Set<SliderCalculation> waiting = new HashSet<>();

    private int value = 100;

    public SliderCalculation(String id) {
        super(id);
    }

    @Override
    protected void calculateOutputValues() {
        if (inputValues[0] > 0.5) {
            waiting.add(this);
        } else {
            waiting.remove(this);
        }

        outputValues[0] = value / 100.0f;
    }

    public void adjust(int delta) {
        value = Math.clamp(value + delta, 0, 100);
    }

    public void reset() {
        value = 100;
    }
}
