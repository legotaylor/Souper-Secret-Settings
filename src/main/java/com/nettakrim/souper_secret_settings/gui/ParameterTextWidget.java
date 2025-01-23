package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterTextWidget extends SuggestionTextFieldWidget {
    protected final String defaultValue;
    protected final ShaderStack stack;

    public ParameterTextWidget(int x, int width, int height, Text message, ShaderStack stack, String defaultValue) {
        super(x, width, height, message, true);
        this.stack = stack;
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            setListeners(this::getParameters, this::setText);
        }
    }

    protected List<String> getParameters() {
        List<String> validUniforms = SouperSecretSettingsClient.soupRenderer.getValidUniforms();

        List<String> parameters = new ArrayList<>(stack.parameterValues.size()+validUniforms.size()+1);

        parameters.addAll(stack.parameterValues.keySet());
        parameters.addAll(validUniforms);

        Collections.sort(parameters);
        if (!defaultValue.isEmpty()) {
            parameters.addFirst(defaultValue);
        }
        return parameters;
    }
}
