package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class HoverButtonWidget extends ButtonWidget {
    protected @Nullable Text hoverText;

    protected HoverButtonWidget(int x, int y, int width, int height, Text message, @Nullable Text hoverText, PressAction onPress) {
        super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.hoverText = hoverText;
    }

    public void setHoverText(@Nullable Text hoverText) {
        this.hoverText = hoverText;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (hovered && hoverText != null && passesRangeCheck(mouseX, mouseY)) {
            SouperSecretSettingsClient.soupGui.setHoverText(hoverText);
        }
    }

    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return true;
    }

    public void setActiveText(@Nullable Text hoverText) {
        setHoverText(hoverText);
        active = hoverText != null;
    }

    public void deselect() {
        setFocused(false);
        hovered = false;
    }
}
