package com.nettakrim.souper_secret_settings.gui.layers;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.LayerRenameAction;
import com.nettakrim.souper_secret_settings.actions.ShaderLoadAction;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    private ButtonWidget saveButton;
    private ButtonWidget loadButton;

    private SuggestionTextFieldWidget nameWidget;

    private static final int infoHeight = 15;

    boolean saveConfirmed;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, getNameText(layer.name), listScreen);
        this.layer = layer;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

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
    protected void createChildren(int x, int width) {
        saveButton = ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.save"), (buttonWidget) -> save()).dimensions(x,0,width/2,20).build();
        loadButton = ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.load"), (buttonWidget) -> load()).dimensions(x + width/2,0,width/2,20).build();

        updateDataButtons();

        nameWidget = new SuggestionTextFieldWidget(x, width, 20, Text.of("layer id"), false);
        nameWidget.setListeners(() -> SouperSecretSettingsClient.soupData.getSavedLayers(true), this::setNameDisambiguate, false);
        nameWidget.submitOnLostFocus = true;
        nameWidget.setText(layer.name);
        nameWidget.setChangedListener(this::setName);

        children.add(nameWidget);
        listScreen.addSelectable(nameWidget);

        children.add(saveButton);
        listScreen.addSelectable(saveButton);
        listScreen.addSelectable(loadButton);
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
            SouperSecretSettingsClient.soupGui.updateActiveLayer();
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
        if (loadButton != null) {
            loadButton.visible = expanded;
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (loadButton != null) {
            loadButton.setY(saveButton.getY());
        }
    }

    private void setName(String name) {
        if (layer.name.equals(name)) return;

        new LayerRenameAction(layer).addToHistory();
        layer.name = name;
        setMessage(getNameText(name));

        updateDataButtons();
        SouperSecretSettingsClient.soupGui.updateActiveLayer();
    }

    private void setNameDisambiguate(String name) {
        new LayerRenameAction(layer).addToHistory();
        layer.name = name;
        layer.disambiguateName();
        setMessage(getNameText(name));

        updateDataButtons();

        nameWidget.setText(layer.name);
        nameWidget.setCursorToEnd(false);
        setMessage(getNameText(layer.name));
    }

    private static Text getNameText(String name) {
        return name.isBlank() ? SouperSecretSettingsClient.translate("gui.unnamed") : Text.literal(name);
    }

    private void save() {
        if (loadButton.active && !saveConfirmed) {
            setConfirm(true);
            return;
        }

        SouperSecretSettingsClient.soupData.saveLayer(layer, this::updateDataButtons);
        updateDataButtons();
        listScreen.recalculateAdditions();
        setConfirm(false);
    }

    private void load() {
        new ShaderLoadAction(layer, layer.name).addToHistory();
        layer.clear();
        SouperSecretSettingsClient.soupData.loadLayer(layer);
        updateDataButtons();
    }

    private void updateDataButtons() {
        saveButton.active = SouperSecretSettingsClient.soupData.isValidName(layer.name);
        loadButton.active = SouperSecretSettingsClient.soupData.savedLayerExists(layer.name) || (layer.name.contains(":") && SouperSecretSettingsClient.soupData.resourceLayers.containsKey(Identifier.tryParse(layer.name)));

        if (saveButton.active && !loadButton.active && layer.isEmpty()) {
            saveButton.active = false;
        }
    }

    @Override
    protected void setExpanded(boolean to) {
        super.setExpanded(to);
        setConfirm(false);
    }

    private void setConfirm(boolean to) {
        saveConfirmed = to;
        saveButton.setMessage(SouperSecretSettingsClient.translate(to ? "gui.confirm" : "gui.save"));
    }

    @Override
    public void onRemove() {
        super.onRemove();
        listScreen.removeSelectable(loadButton);
    }

    @Override
    protected int getEditState() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer == layer ? 2 : 1;
    }
}
