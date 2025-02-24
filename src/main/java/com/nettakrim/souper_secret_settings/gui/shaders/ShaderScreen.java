package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.OverrideManager;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShaderScreen extends ListScreen<ShaderData> {
    public final ShaderLayer layer;
    public final Identifier registry;
    public final Identifier[] customPasses;

    public ShaderScreen(int scrollIndex, ShaderLayer layer, Identifier registry, Identifier[] customPasses) {
        super(scrollIndex);
        this.layer = layer;
        this.registry = registry;
        this.customPasses = customPasses;
    }

    @Override
    protected List<ShaderData> getListValues() {
        return layer.getList(registry);
    }

    @Override
    protected ListWidget createListWidget(ShaderData value) {
        return new ShaderWidget(layer, value, this, listX, listWidth);
    }

    @Override
    public List<String> calculateAdditions() {
        List<String> shaders = new ArrayList<>(Shaders.getRegistry(registry).size());

        for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(registry)) {
            shaders.add(shaderRegistry.getID().toString());
        }

        Collections.sort(shaders);

        Map<String, List<ShaderRegistryEntry>> registryGroups = SouperSecretSettingsClient.soupRenderer.shaderGroups.get(registry);
        if (registryGroups != null) {
            List<String> random = new ArrayList<>(registryGroups.keySet().size()+1);
            for (String s : registryGroups.keySet()) {
                random.add("random_"+s);
            }
            if (shaders.size() > 1) {
                random.add("random");
            }
            Collections.sort(random);
            shaders.addAll(random);
        } else if (shaders.size() > 1){
            shaders.add("random");
        }

        return shaders;
    }

    @Override
    public ShaderData tryGetAddition(String addition) {
        Identifier identifier = Shaders.guessPostShader(addition);
        if (identifier != null) {
            List<ShaderData> shader = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, identifier, 1, layer);
            if (shader != null) {
                return shader.getFirst();
            }
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        OverrideManager.currentShaderIndex = 0;
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected boolean canUseRandom() {
        return true;
    }

    @Override
    protected Text getAdditionText(String addition) {
        if (addition.startsWith("random")) {
            return super.getAdditionText(addition);
        }

        return Text.translatableWithFallback("gui.luminance.shader."+addition.replace(':','.'), addition);
    }
}
