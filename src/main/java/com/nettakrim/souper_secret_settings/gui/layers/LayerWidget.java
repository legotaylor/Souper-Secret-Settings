package com.nettakrim.souper_secret_settings.gui.layers;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    private static final int infoHeight = 15;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(layer.name), listScreen);
        this.layer = layer;


        SuggestionTextFieldWidget nameWidget = new SuggestionTextFieldWidget(x, width, 20, Text.of("layer name"), false);
        nameWidget.setText(layer.name);
        nameWidget.setChangedListener((s) -> {
            layer.name = s;
            setMessage(Text.literal(s));
        });

        children.add(nameWidget);
        listScreen.addSelectable(nameWidget);
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

        Text[] info = layer.getInfo();
        int infoPos = this.getY()+collapseHeight - info.length*infoHeight - 2;
        for (Text text : info) {
            int next = infoPos + infoHeight;
            drawScrollableText(context, ClientData.minecraft.textRenderer, text, this.getX(), infoPos, this.getX() + this.getWidth(), next, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
            infoPos = next;
        }
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
    public void onClick(double mouseX, double mouseY) {
        int distance = getX()+getWidth() - (int)mouseX;
        if (distance < 20 && distance > 10) {
            SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
            return;
        }
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void updateCollapse(int y) {
        super.updateCollapse(y);
        if (expanded) {
            collapseHeight += layer.getInfo().length * infoHeight;
        }
    }
}
