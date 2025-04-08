package com.nettakrim.souper_secret_settings;

import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> SouperSecretSettingsClient.soupGui.getScreen(SoupGui.ScreenType.OPTION, true);
    }
}
