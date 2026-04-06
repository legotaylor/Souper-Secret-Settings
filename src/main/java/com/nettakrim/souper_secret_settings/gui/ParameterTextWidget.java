package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ParameterTextWidget extends SuggestionTextFieldWidget {
    protected final String defaultValue;
    protected final ShaderLayer layer;

    public ParameterTextWidget(int x, int width, int height, Component message, ShaderLayer layer, String defaultValue) {
        super(x, width, height, message, true);
        this.layer = layer;
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            setListeners(this::getParameters, this::setValue, false);
        }
    }

    protected List<String> getParameters() {
        List<Identifier> validUniforms = SouperSecretSettingsClient.soupRenderer.getValidUniforms();

        List<String> parameters = new ArrayList<>(layer.parameterValues.size()+validUniforms.size()+1);

        parameters.addAll(layer.parameterValues.keySet());
        parameters.addAll(validUniforms.stream().map(Identifier::toString).toList());

        Collections.sort(parameters);
        if (!defaultValue.isEmpty()) {
            parameters.addFirst(defaultValue);
        }
        return parameters;
    }
}
