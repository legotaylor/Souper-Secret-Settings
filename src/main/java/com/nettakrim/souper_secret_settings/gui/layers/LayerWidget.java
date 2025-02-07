package com.nettakrim.souper_secret_settings.gui.layers;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.text.Text;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(layer.name), listScreen);
        this.layer = layer;
    }

    @Override
    public boolean isActive() {
        return layer.active;
    }

    @Override
    public void setActive(boolean to) {
        layer.active = to;
    }

    @Override
    protected void setExpanded(boolean to) {
        SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
    }
}
