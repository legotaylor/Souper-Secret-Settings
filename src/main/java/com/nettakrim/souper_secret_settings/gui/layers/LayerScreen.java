package com.nettakrim.souper_secret_settings.gui.layers;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LayerScreen extends ListScreen<ShaderLayer> {
    public LayerScreen(int scrollIndex) {
        super(scrollIndex);
    }

    @Override
    protected List<ShaderLayer> getListValues() {
        return SouperSecretSettingsClient.soupRenderer.shaderLayers;
    }

    @Override
    protected ListWidget createListWidget(ShaderLayer value) {
        return new LayerWidget(value, this, listX, listWidth);
    }

    @Override
    public List<String> calculateAdditions() {
        return SouperSecretSettingsClient.soupData.getSavedLayers();
    }

    @Override
    public @Nullable ShaderLayer tryGetAddition(String addition) {
        ShaderLayer shaderLayer = new ShaderLayer(addition);
        SouperSecretSettingsClient.soupRenderer.activeLayer = shaderLayer;
        SouperSecretSettingsClient.soupGui.updateActiveLayer();
        return shaderLayer;
    }

    @Override
    public void removeEntry(ListWidget listWidget) {
        super.removeEntry(listWidget);
        if (SouperSecretSettingsClient.soupRenderer.activeLayer != ((LayerWidget)listWidget).layer) {
            return;
        }

        if (SouperSecretSettingsClient.soupRenderer.shaderLayers.isEmpty()) {
            SouperSecretSettingsClient.soupRenderer.clearAll();
            SouperSecretSettingsClient.soupRenderer.loadDefault();
            SouperSecretSettingsClient.soupGui.open(SoupGui.ScreenType.LAYERS);
        } else {
            SouperSecretSettingsClient.soupRenderer.activeLayer = SouperSecretSettingsClient.soupRenderer.shaderLayers.getLast();
            SouperSecretSettingsClient.soupGui.updateActiveLayer();
        }
    }

    @Override
    protected boolean canRemoveAdditions() {
        return true;
    }

    @Override
    protected void removeAddition(String addition) {
        SouperSecretSettingsClient.soupData.deleteSavedLayer(addition);
        super.removeAddition(addition);
    }

    @Override
    protected boolean canUseRandom() {
        return false;
    }
}
