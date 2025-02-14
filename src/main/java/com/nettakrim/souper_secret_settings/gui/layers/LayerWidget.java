package com.nettakrim.souper_secret_settings.gui.layers;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.LayerRenameAction;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    private final ButtonWidget saveButton;
    private final ButtonWidget loadButton;

    private static final int infoHeight = 15;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, getNameText(layer.name), listScreen);
        this.layer = layer;

        saveButton = ButtonWidget.builder(Text.literal("save"), (buttonWidget) -> save()).dimensions(x,0,width/2,20).build();
        loadButton = ButtonWidget.builder(Text.literal("load"), (buttonWidget) -> load()).dimensions(x + width/2,0,width/2,20).build();

        SuggestionTextFieldWidget nameWidget = new SuggestionTextFieldWidget(x, width, 20, Text.of("layer id"), false);
        nameWidget.setListeners(SouperSecretSettingsClient.soupData::getSavedLayers, null);
        nameWidget.setText(layer.name);
        nameWidget.setChangedListener(this::setName);

        children.add(nameWidget);
        listScreen.addSelectable(nameWidget);

        children.add(saveButton);
        listScreen.addSelectable(saveButton);
        listScreen.addSelectable(loadButton);

        updateDataButtons();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        // TODO: replace with icons
        String indicator = SouperSecretSettingsClient.soupRenderer.activeLayer == layer ? "!" : "/";
        int buttonStart = this.getX()+getWidth()-20;
        drawScrollableText(context, ClientData.minecraft.textRenderer, Text.literal(indicator), buttonStart, this.getY(), buttonStart+10, getY()+getHeight(), (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);

        if (!expanded) {
            return;
        }
        loadButton.render(context, mouseX, mouseY, delta);

        Text[] info = layer.getInfo();
        int infoPos = this.getY()+collapseHeight - info.length*infoHeight - 1;
        for (Text text : info) {
            int next = infoPos + infoHeight;
            drawScrollableText(context, ClientData.minecraft.textRenderer, text, this.getX(), infoPos, this.getX() + this.getWidth(), next, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
            infoPos = next;
        }
    }

    @Override
    protected boolean getStoredExpanded() {
        return layer.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        layer.expanded = to;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int distance = getX()+getWidth() - (int)mouseX;
        if (distance < 20 && distance > 10) {
            SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
            return;
        }
        super.onClick(mouseX, mouseY);
    }

    @Override
    protected Toggleable getToggleable() {
        return layer;
    }

    @Override
    public void updateCollapse(int y) {
        super.updateCollapse(y);
        if (expanded) {
            collapseHeight += layer.getInfo().length * infoHeight;
            loadButton.setY(saveButton.getY());
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        loadButton.setY(saveButton.getY());
    }

    private void setName(String name) {
        if (layer.name.equals(name)) return;

        new LayerRenameAction(layer).addToHistory();
        layer.name = name;
        setMessage(getNameText(name));

        updateDataButtons();
    }

    private static Text getNameText(String name) {
        return Text.literal(name.isBlank() ? "<unnamed>" : name);
    }

    private void save() {
        SouperSecretSettingsClient.soupData.saveLayer(layer, this::updateDataButtons);
        updateDataButtons();
    }

    private void load() {
        layer.clear();
        SouperSecretSettingsClient.soupData.loadLayer(layer);
        updateDataButtons();
    }

    private void updateDataButtons() {
        saveButton.active = SouperSecretSettingsClient.soupData.isValidName(layer.name);
        loadButton.active = SouperSecretSettingsClient.soupData.getLayerPath(layer).toFile().exists();
    }
}
