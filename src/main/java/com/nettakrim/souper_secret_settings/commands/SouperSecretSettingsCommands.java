package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.brigadier.tree.RootCommandNode;

import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SouperSecretSettingsCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            new ShaderListCommand("shader", Shaders.getMainRegistryId(), (stack) -> stack.shaderDatas).register(root);
            new ShaderListCommand("effect", SoupRenderer.layerEffectRegistry, (stack) -> stack.layerEffects).register(root);
        });
    }
}
