package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.parameters.ParameterScreen;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackScreen extends ListScreen<ShaderData> {
    public final ShaderStack stack;

    public StackScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    protected ButtonWidget getToggleButton() {
        return ButtonWidget.builder(Text.literal("parameters"), (widget) -> SouperSecretSettingsClient.client.setScreen(new ParameterScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()))).dimensions(listGap, listGap, 100, headerHeight).build();
    }

    @Override
    protected List<ShaderData> getListValues() {
        return stack.shaderDatas;
    }

    @Override
    protected ListWidget createListWidget(ShaderData value) {
        return new ShaderWidget(stack, value, this, listX, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        List<String> shaders = new ArrayList<>(Shaders.getRegistry().size()+1);

        for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry()) {
            shaders.add(shaderRegistry.getID().toString());
        }
        if (shaders.size() > 1) {
            shaders.add("random");
        }

        Collections.sort(shaders);

        return shaders;
    }

    @Override
    public ShaderData tryGetAddition(String addition) {
        Identifier identifier = Shaders.guessPostShader(addition);
        if (identifier != null) {
            if (SouperSecretSettingsClient.soupRenderer.addShaders(Shaders.getMainRegistryId(), identifier, 1, stack, stack::addShaderData)) {
                return stack.shaderDatas.removeLast();
            }
        }
        return null;
    }
}
