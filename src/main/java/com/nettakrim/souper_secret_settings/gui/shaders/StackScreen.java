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
    public final Identifier registry;
    public final Identifier[] customPasses;

    public StackScreen(ShaderStack stack, Identifier registry, Identifier[] customPasses) {
        super(Text.literal(""));
        this.stack = stack;
        this.registry = registry;
        this.customPasses = customPasses;
    }

    @Override
    protected ButtonWidget getToggleButton() {
        return ButtonWidget.builder(Text.literal("parameters"), (widget) -> SouperSecretSettingsClient.client.setScreen(new ParameterScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()))).dimensions(listGap, listGap, 100, headerHeight).build();
    }

    @Override
    protected List<ShaderData> getListValues() {
        return stack.getList(registry);
    }

    @Override
    protected ListWidget createListWidget(ShaderData value) {
        return new ShaderWidget(stack, value, this, listX, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        List<String> shaders = new ArrayList<>(Shaders.getRegistry(registry).size()+1);

        for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(registry)) {
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
            List<ShaderData> shader = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, identifier, 1, stack);
            if (shader != null) {
                return shader.getFirst();
            }
        }
        return null;
    }
}
