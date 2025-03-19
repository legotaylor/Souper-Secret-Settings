package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.keybindings.KeybindingHelper;
import com.nettakrim.souper_secret_settings.commands.OptionCommand;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding openGUI = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "open_gui", GLFW.GLFW_KEY_K);
    public static final KeyBinding toggleSoup = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "toggle_soup", GLFW.GLFW_KEY_UNKNOWN);

    public static void tick() {
        if (openGUI.wasPressed()) {
            SouperSecretSettingsClient.soupGui.open(SouperSecretSettingsClient.soupGui.getCurrentScreenType());
        }
        if (toggleSoup.wasPressed()) {
            OptionCommand.toggle(false);
        }
    }
}
