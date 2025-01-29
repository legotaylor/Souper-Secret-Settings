package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.keybindings.KeybindingHelper;
import com.nettakrim.souper_secret_settings.gui.shaders.StackScreen;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding openGUI;
    static {
        openGUI = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "open_gui", GLFW.GLFW_KEY_K);
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (ClientData.minecraft.isFinishedLoading()) {
                tick();
            }
        });
    }

    public static void tick() {
        if (openGUI.wasPressed()) {

            ClientData.minecraft.setScreen(new StackScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack(), SoupRenderer.layerEffectRegistry, new Identifier[] {
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_stack_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_stack_render")
            }));
        }
    }
}
