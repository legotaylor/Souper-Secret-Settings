package com.nettakrim.souper_secret_settings.gui.config;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    public ConfigScreen() {
        super(Text.literal(""));
    }

    @Override
    protected void init() {
        for (ClickableWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addDrawableChild(clickableWidget);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        SouperSecretSettingsClient.soupGui.drawCurrentHoverText(context, mouseX, mouseY);
    }

    @Override
    public void close() {
        SouperSecretSettingsClient.soupGui.onClose();
    }
}
