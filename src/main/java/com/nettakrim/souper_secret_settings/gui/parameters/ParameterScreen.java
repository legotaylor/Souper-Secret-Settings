package com.nettakrim.souper_secret_settings.gui.parameters;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterScreen extends ListScreen<Calculation> {
    public final ShaderLayer layer;

    public ParameterScreen(int scrollIndex, ShaderLayer layer) {
        super(scrollIndex);
        this.layer = layer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ShaderTime.currentRenderType = SouperSecretSettingsClient.soupRenderer.getRenderType();
        super.render(context, mouseX, mouseY, delta);
        ShaderTime.currentRenderType = Shader.RenderType.UI;
    }

    @Override
    protected List<Calculation> getListValues() {
        return layer.calculations;
    }

    @Override
    protected ListWidget createListWidget(Calculation value) {
        return new ParameterWidget(value, layer, this, SoupGui.listX, SoupGui.listWidth);
    }

    @Override
    public List<String> calculateAdditions() {
        List<String> calculations = new ArrayList<>(Calculations.getIds());
        Collections.sort(calculations);
        return calculations;
    }

    @Override
    public Calculation tryGetAddition(String addition) {
        return Calculations.createCalculation(addition);
    }

    @Override
    protected boolean canUseRandom() {
        return false;
    }

    @Override
    protected boolean canPreview() {
        return false;
    }

    @Override
    protected boolean matchIdentifiers() {
        return false;
    }
}
