package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.SpectatorHandler;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class SoupSpectateHandler implements SpectatorHandler {
    public ShaderLayer shaderLayer;

    @Override
    public int getPriority(Entity entity) {
        return SouperSecretSettingsClient.soupData.resourceLayers.containsKey(getID(entity)) ? 100 : -1;
    }

    @Override
    public void apply(Entity entity) {
        shaderLayer = new ShaderLayer(getID(entity).toString());
    }

    private Identifier getID(Entity entity) {
        String s = entity.getType().toString().substring(7);
        int i = s.indexOf('.');
        return Identifier.of(s.substring(0, i), s.substring(i+1));
    }

    @Override
    public void clear() {
        shaderLayer = null;
    }
}
