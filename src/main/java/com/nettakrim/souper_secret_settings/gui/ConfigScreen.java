package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    protected ConfigScreen() {
        super(Text.literal(""));
    }

    @Override
    public void close() {
        SouperSecretSettingsClient.soupGui.onClose();
    }
}
