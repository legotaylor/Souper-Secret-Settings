package com.nettakrim.souper_secret_settings.shaders.calculations.key;

import com.nettakrim.souper_secret_settings.gui.parameters.KeyInputButton;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class KeyCalculation extends Calculation {
    public KeyCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{""};
    }

    @Override
    protected String[] getInputNames() {
        return new String[]{"key"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    public AbstractWidget createWidget(int x, int width, int index, ShaderLayer layer, String currentValue, Consumer<String> responder) {
        KeyInputButton keyInputButton = new KeyInputButton(x, width, 20, Component.literal(inputNames[index]));
        keyInputButton.setValue(currentValue);
        keyInputButton.setResponder(responder);
        return keyInputButton;
    }
}
