package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.keybindings.KeybindingHelper;
import com.nettakrim.souper_secret_settings.commands.OptionCommand;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding openGUI = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "open_gui", GLFW.GLFW_KEY_U);
    public static final KeyBinding toggleSoup = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "toggle_soup", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding clear = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "clear", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding undo = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "undo", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding redo = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "redo", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding random = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "random", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyBinding sound = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "sound", GLFW.GLFW_KEY_UNKNOWN);

    public static void tick() {
        if (openGUI.wasPressed()) {
            SouperSecretSettingsClient.soupGui.open(SouperSecretSettingsClient.soupGui.getCurrentScreenType(), true);
        }
        if (toggleSoup.wasPressed()) {
            OptionCommand.toggle(false);
        }
        if (undo.wasPressed()) {
            SouperSecretSettingsClient.actions.undo();
        }
        if (redo.wasPressed()) {
            SouperSecretSettingsClient.actions.redo();
        }
        if (clear.wasPressed()) {
            SouperSecretSettingsCommands.layerCommand.removeAll(null);
        }
        if (random.wasPressed()) {
            SouperSecretSettingsCommands.shaderCommand.add(Identifier.of("random_edible"), 1, -1, true);
        }
        if (sound.wasPressed()) {
            RandomSound.play();
        }
    }
}
