package com.nettakrim.souper_secret_settings.gui.layers;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(layer.name), listScreen);
        this.layer = layer;

        ClickableWidget info = ButtonWidget.builder(Text.literal("shaders: "+layer.shaderDatas.size()+" effects: "+layer.layerEffects.size()), (buttonWidget) -> {}).dimensions(x, 0, width, 12).build();
        children.add(info);
        listScreen.addSelectable(info);

        ClickableWidget select = ButtonWidget.builder(Text.literal("set active"), (buttonWidget) -> SouperSecretSettingsClient.soupRenderer.activeLayer = layer).dimensions(x, 0, width, 20).build();
        children.add(select);
        listScreen.addSelectable(select);
    }

    @Override
    public boolean isActive() {
        return layer.active;
    }

    @Override
    public void setActive(boolean to) {
        layer.active = to;
    }
}
