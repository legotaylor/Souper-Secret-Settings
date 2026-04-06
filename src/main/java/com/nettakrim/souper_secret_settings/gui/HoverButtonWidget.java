package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverButtonWidget extends Button {
    protected @Nullable net.minecraft.network.chat.Component hoverText;

    protected HoverButtonWidget(int x, int y, int width, int height, net.minecraft.network.chat.Component message, @Nullable net.minecraft.network.chat.Component hoverText, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.hoverText = hoverText;
    }

    public void setHoverText(@Nullable net.minecraft.network.chat.Component hoverText) {
        this.hoverText = hoverText;
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (isHovered && hoverText != null && passesRangeCheck(mouseX, mouseY)) {
            SouperSecretSettingsClient.soupGui.setHoverText(hoverText);
        }
    }

    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return true;
    }

    public void setActiveText(@Nullable net.minecraft.network.chat.Component hoverText) {
        setHoverText(hoverText);
        active = hoverText != null;
    }

    public void deselect() {
        setFocused(false);
        isHovered = false;
    }
}
