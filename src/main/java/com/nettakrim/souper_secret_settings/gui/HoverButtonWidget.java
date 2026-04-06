package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.jetbrains.annotations.Nullable;

public class HoverButtonWidget extends ButtonWidget {
    protected @Nullable net.minecraft.text.Text hoverText;

    protected HoverButtonWidget(int x, int y, int width, int height, net.minecraft.text.Text message, @Nullable net.minecraft.text.Text hoverText, PressAction onPress) {
        super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.hoverText = hoverText;
    }

    public void setHoverText(@Nullable net.minecraft.text.Text hoverText) {
        this.hoverText = hoverText;
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        if (hovered && hoverText != null && passesRangeCheck(mouseX, mouseY)) {
            SouperSecretSettingsClient.soupGui.setHoverText(hoverText);
        }
    }

    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return true;
    }

    public void setActiveText(@Nullable net.minecraft.text.Text hoverText) {
        setHoverText(hoverText);
        active = hoverText != null;
    }

    public void deselect() {
        setFocused(false);
        hovered = false;
    }
}
